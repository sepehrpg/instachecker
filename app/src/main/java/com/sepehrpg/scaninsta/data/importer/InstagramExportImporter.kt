package com.sepehrpg.scaninsta.data.importer

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.util.Locale
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * Locates and parses follower/following files anywhere inside an Instagram ZIP export.
 *
 * No archive entry is extracted to disk. This avoids path-traversal risks and keeps the user's
 * private export inside the app process. HTML and JSON are Meta's documented export formats;
 * XML is accepted as a best-effort compatibility format.
 */
class InstagramExportImporter @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    fun importZip(
        inputStream: InputStream,
        settings: ExportFileSettings = ExportFileSettings(),
    ): InstagramExportData {
        validateSettings(settings)

        val candidates = try {
            readCandidates(inputStream, settings)
        } catch (exception: InstagramExportException) {
            throw exception
        } catch (exception: ZipException) {
            throw InstagramExportException("The selected file is not a readable ZIP archive.", exception)
        } catch (exception: Exception) {
            throw InstagramExportException("The ZIP archive could not be read.", exception)
        }

        val followerFiles = selectBestMatches(candidates, FileRole.FOLLOWERS)
        val followingFiles = selectBestMatches(candidates, FileRole.FOLLOWING)

        if (followerFiles.isEmpty() || followingFiles.isEmpty()) {
            val missing = buildList {
                if (followerFiles.isEmpty()) add("followers")
                if (followingFiles.isEmpty()) add("following")
            }.joinToString(" and ")
            throw InstagramExportException(
                "Could not find the $missing file in this export. Check the file names in Import settings.",
            )
        }

        val followers = followerFiles.flatMap { parseAccounts(it) }.distinctByUsername()
        val following = followingFiles.flatMap { parseAccounts(it) }.distinctByUsername()

        return InstagramExportData(
            followers = followers,
            following = following,
            sourceFiles = (followerFiles + followingFiles).map { it.archivePath }.distinct(),
        )
    }

    private fun validateSettings(settings: ExportFileSettings) {
        val followersName = normalizedStem(settings.followersFileName)
        val followingName = normalizedStem(settings.followingFileName)
        if (followersName.isBlank() || followingName.isBlank()) {
            throw InstagramExportException("Follower and following file names cannot be empty.")
        }
        if (followersName == followingName) {
            throw InstagramExportException("Follower and following file names must be different.")
        }
    }

    private fun readCandidates(
        inputStream: InputStream,
        settings: ExportFileSettings,
    ): List<ArchiveCandidate> {
        val candidates = mutableListOf<ArchiveCandidate>()
        var totalUncompressedBytes = 0L
        var entryCount = 0

        ZipInputStream(BufferedInputStream(inputStream)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entryCount += 1
                if (entryCount > MAX_ARCHIVE_ENTRIES) {
                    throw InstagramExportException("The ZIP archive contains too many files.")
                }

                if (!entry.isDirectory && supportedExtension(entry.name) != null) {
                    val content = readEntry(zip) { bytesRead ->
                        totalUncompressedBytes += bytesRead
                        if (totalUncompressedBytes > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                            throw InstagramExportException("The ZIP archive is too large to process safely.")
                        }
                    }
                    classify(entry.name, content, settings)?.let(candidates::add)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        if (entryCount == 0) {
            throw InstagramExportException("The selected ZIP archive is empty or unreadable.")
        }
        return candidates
    }

    private fun readEntry(zip: ZipInputStream, onBytesRead: (Long) -> Unit): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var entryBytes = 0L
        var count = zip.read(buffer)
        while (count >= 0) {
            if (count > 0) {
                entryBytes += count
                if (entryBytes > MAX_ENTRY_UNCOMPRESSED_BYTES) {
                    throw InstagramExportException("A relationship file in the ZIP archive is too large.")
                }
                onBytesRead(count.toLong())
                output.write(buffer, 0, count)
            }
            count = zip.read(buffer)
        }
        return output.toString(Charsets.UTF_8.name())
    }

    private fun classify(
        archivePath: String,
        content: String,
        settings: ExportFileSettings,
    ): ArchiveCandidate? {
        val stem = normalizedStem(archivePath)
        val configuredFollowers = normalizedStem(settings.followersFileName)
        val configuredFollowing = normalizedStem(settings.followingFileName)

        val roleAndPriority = when {
            matchesConfiguredSeries(stem, configuredFollowers) -> FileRole.FOLLOWERS to PRIORITY_CONFIGURED
            matchesConfiguredSeries(stem, configuredFollowing) -> FileRole.FOLLOWING to PRIORITY_CONFIGURED
            KNOWN_FOLLOWERS_FILE.matches(stem) -> FileRole.FOLLOWERS to PRIORITY_KNOWN_NAME
            KNOWN_FOLLOWING_FILE.matches(stem) -> FileRole.FOLLOWING to PRIORITY_KNOWN_NAME
            else -> detectRoleFromContent(content, supportedExtension(archivePath) ?: return null)
                ?.let { it to PRIORITY_CONTENT }
                ?: return null
        }

        return ArchiveCandidate(
            archivePath = archivePath,
            content = content,
            format = supportedExtension(archivePath) ?: return null,
            role = roleAndPriority.first,
            priority = roleAndPriority.second,
        )
    }

    private fun selectBestMatches(
        candidates: List<ArchiveCandidate>,
        role: FileRole,
    ): List<ArchiveCandidate> {
        val matches = candidates.filter { it.role == role }
        val bestPriority = matches.maxOfOrNull { it.priority } ?: return emptyList()
        return matches.filter { it.priority == bestPriority }.sortedBy { it.archivePath }
    }

    private fun detectRoleFromContent(content: String, format: ExportFormat): FileRole? =
        when (format) {
            ExportFormat.JSON -> detectJsonRole(content)
            ExportFormat.HTML, ExportFormat.XML -> detectMarkupRole(content)
        }

    private fun detectJsonRole(content: String): FileRole? = runCatching {
        val root = json.parseToJsonElement(content) as? JsonObject ?: return@runCatching null
        when {
            root.keys.any { it.equals("relationships_following", ignoreCase = true) } -> FileRole.FOLLOWING
            root.keys.any { it.equals("relationships_followers", ignoreCase = true) } -> FileRole.FOLLOWERS
            else -> null
        }
    }.getOrNull()

    private fun detectMarkupRole(content: String): FileRole? {
        val labels = DOCUMENT_LABEL.findAll(content)
            .map { stripTags(decodeEntities(it.groupValues[1])).trim().lowercase(Locale.ROOT) }
            .toList()
        return when {
            labels.any { it == "following" || it.contains("accounts you follow") } -> FileRole.FOLLOWING
            labels.any { it == "followers" || it.contains("accounts following you") } -> FileRole.FOLLOWERS
            RELATIONSHIPS_FOLLOWING_TAG.containsMatchIn(content) -> FileRole.FOLLOWING
            RELATIONSHIPS_FOLLOWERS_TAG.containsMatchIn(content) -> FileRole.FOLLOWERS
            else -> null
        }
    }

    private fun parseAccounts(candidate: ArchiveCandidate): List<InstagramAccount> = try {
        when (candidate.format) {
            ExportFormat.JSON -> parseJsonAccounts(candidate.content)
            ExportFormat.HTML -> parseHtmlAccounts(candidate.content)
            ExportFormat.XML -> parseXmlAccounts(candidate.content)
        }
    } catch (exception: Exception) {
        throw InstagramExportException(
            "Could not parse ${candidate.archivePath}. The export file may be incomplete or unsupported.",
            exception,
        )
    }

    private fun parseJsonAccounts(content: String): List<InstagramAccount> {
        val accounts = mutableListOf<InstagramAccount>()

        fun visit(element: JsonElement) {
            when (element) {
                is JsonArray -> element.forEach(::visit)
                is JsonObject -> {
                    val relationshipData = element["string_list_data"] as? JsonArray
                    if (relationshipData != null) {
                        relationshipData.forEach { item ->
                            val data = item as? JsonObject ?: return@forEach
                            val username = data["value"]?.jsonPrimitive?.contentOrNull
                            val profileUrl = data["href"]?.jsonPrimitive?.contentOrNull
                            createAccount(username, profileUrl)?.let(accounts::add)
                        }
                    } else {
                        element.values.forEach(::visit)
                    }
                }
                else -> Unit
            }
        }

        visit(json.parseToJsonElement(content))
        return accounts
    }

    private fun parseHtmlAccounts(content: String): List<InstagramAccount> =
        ANCHOR.findAll(content).mapNotNull { anchor ->
            val attributes = anchor.groupValues[1]
            val href = HREF.find(attributes)?.groupValues?.get(2) ?: return@mapNotNull null
            val visibleText = stripTags(decodeEntities(anchor.groupValues[2])).trim()
            createProfileLinkAccount(visibleText, href)
        }.toList()

    private fun parseXmlAccounts(content: String): List<InstagramAccount> {
        val linkedAccounts = parseHtmlAccounts(content)
        if (linkedAccounts.isNotEmpty()) return linkedAccounts

        val usernames = XML_VALUE.findAll(content).map { decodeEntities(it.groupValues[1]).trim() }.toList()
        val profileUrls = XML_HREF.findAll(content).map { decodeEntities(it.groupValues[1]).trim() }.toList()
        return usernames.mapIndexedNotNull { index, username ->
            createAccount(username, profileUrls.getOrNull(index))
        }
    }

    private fun createProfileLinkAccount(rawUsername: String?, rawUrl: String?): InstagramAccount? {
        val usernameFromUrl = usernameFromProfileUrl(rawUrl)
        val username = normalizeUsername(rawUsername).takeIf { it != null && usernameFromUrl != null }
            ?: usernameFromUrl
        return createAccount(username, rawUrl)
    }

    private fun createAccount(rawUsername: String?, rawUrl: String?): InstagramAccount? {
        val username = normalizeUsername(rawUsername) ?: usernameFromProfileUrl(rawUrl) ?: return null
        val profileUrl = rawUrl?.trim()?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
            ?: "https://www.instagram.com/$username/"
        return InstagramAccount(username = username, profileUrl = profileUrl)
    }

    private fun normalizeUsername(value: String?): String? {
        val candidate = value?.trim()?.removePrefix("@") ?: return null
        return candidate.takeIf { INSTAGRAM_USERNAME.matches(it) }
    }

    private fun usernameFromProfileUrl(rawUrl: String?): String? = runCatching {
        val uri = URI(decodeEntities(rawUrl.orEmpty().trim()))
        val host = uri.host?.lowercase(Locale.ROOT)?.removePrefix("www.") ?: return@runCatching null
        if (host != "instagram.com" && !host.endsWith(".instagram.com")) return@runCatching null
        val segments = uri.path.orEmpty().split('/').filter(String::isNotBlank)
        val candidate = if (segments.firstOrNull() == "_u") segments.getOrNull(1) else segments.firstOrNull()
        candidate?.takeUnless { it.lowercase(Locale.ROOT) in RESERVED_INSTAGRAM_PATHS }
            ?.let(::normalizeUsername)
    }.getOrNull()

    private fun List<InstagramAccount>.distinctByUsername(): List<InstagramAccount> =
        distinctBy { it.username.lowercase(Locale.ROOT) }

    private fun supportedExtension(path: String): ExportFormat? =
        when (path.substringAfterLast('.', missingDelimiterValue = "").lowercase(Locale.ROOT)) {
            "json" -> ExportFormat.JSON
            "html", "htm" -> ExportFormat.HTML
            "xml" -> ExportFormat.XML
            else -> null
        }

    private fun normalizedStem(pathOrName: String): String {
        val fileName = pathOrName.replace('\\', '/').substringAfterLast('/')
        return fileName
            .substringBeforeLast('.', missingDelimiterValue = fileName)
            .trim()
            .lowercase(Locale.ROOT)
    }

    private fun matchesConfiguredSeries(stem: String, configuredStem: String): Boolean {
        if (stem == configuredStem) return true
        val numberedMatch = NUMBERED_FILE.matchEntire(configuredStem) ?: return false
        val prefix = Regex.escape(numberedMatch.groupValues[1])
        return Regex("^${prefix}_[0-9]+$").matches(stem)
    }

    private fun decodeEntities(value: String): String {
        var decoded = value
            .replace("&amp;", "&", ignoreCase = true)
            .replace("&quot;", "\"", ignoreCase = true)
            .replace("&#39;", "'", ignoreCase = true)
            .replace("&apos;", "'", ignoreCase = true)
            .replace("&lt;", "<", ignoreCase = true)
            .replace("&gt;", ">", ignoreCase = true)
        decoded = HEX_ENTITY.replace(decoded) { match ->
            match.groupValues[1].toIntOrNull(16)?.let(::codePointToString) ?: match.value
        }
        return DECIMAL_ENTITY.replace(decoded) { match ->
            match.groupValues[1].toIntOrNull()?.let(::codePointToString) ?: match.value
        }
    }

    private fun codePointToString(codePoint: Int): String =
        runCatching { String(Character.toChars(codePoint)) }.getOrDefault("")

    private fun stripTags(value: String): String = TAG.replace(value, "")

    private data class ArchiveCandidate(
        val archivePath: String,
        val content: String,
        val format: ExportFormat,
        val role: FileRole,
        val priority: Int,
    )

    private enum class FileRole { FOLLOWERS, FOLLOWING }
    private enum class ExportFormat { JSON, HTML, XML }

    private companion object {
        const val MAX_ARCHIVE_ENTRIES = 10_000
        const val MAX_ENTRY_UNCOMPRESSED_BYTES = 50L * 1024L * 1024L
        const val MAX_TOTAL_UNCOMPRESSED_BYTES = 100L * 1024L * 1024L
        const val PRIORITY_CONTENT = 1
        const val PRIORITY_KNOWN_NAME = 2
        const val PRIORITY_CONFIGURED = 3

        val KNOWN_FOLLOWERS_FILE = Regex("^followers(?:_[0-9]+)?$")
        val KNOWN_FOLLOWING_FILE = Regex("^following(?:_[0-9]+)?$")
        val NUMBERED_FILE = Regex("^(.+)_([0-9]+)$")
        val INSTAGRAM_USERNAME = Regex("^[A-Za-z0-9._]{1,30}$")
        val ANCHOR = Regex("""<a\b([^>]*)>(.*?)</a\s*>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val HREF = Regex("""\bhref\s*=\s*(["'])(.*?)\1""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val DOCUMENT_LABEL = Regex("""<(?:title|h1|heading)\b[^>]*>(.*?)</(?:title|h1|heading)\s*>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val RELATIONSHIPS_FOLLOWING_TAG = Regex("""<relationships_following\b""", RegexOption.IGNORE_CASE)
        val RELATIONSHIPS_FOLLOWERS_TAG = Regex("""<relationships_followers\b""", RegexOption.IGNORE_CASE)
        val XML_VALUE = Regex("""<value\b[^>]*>(.*?)</value\s*>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val XML_HREF = Regex("""<href\b[^>]*>(.*?)</href\s*>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val TAG = Regex("<[^>]+>")
        val HEX_ENTITY = Regex("&#x([0-9a-fA-F]+);")
        val DECIMAL_ENTITY = Regex("&#([0-9]+);")
        val RESERVED_INSTAGRAM_PATHS = setOf(
            "about", "accounts", "developer", "directory", "explore", "legal", "privacy", "reels", "stories",
        )
    }
}
