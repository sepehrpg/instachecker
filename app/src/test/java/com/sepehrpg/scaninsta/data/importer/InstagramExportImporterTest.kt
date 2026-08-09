package com.sepehrpg.scaninsta.data.importer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class InstagramExportImporterTest {
    private val importer = InstagramExportImporter()

    @Test
    fun `finds nested JSON files, combines follower parts, and ignores unrelated files`() {
        val archive = zipOf(
            "account/connections/followers_and_following/followers_1.json" to followersJson("alice"),
            "account/connections/followers_and_following/followers_2.json" to followersJson("bob"),
            "account/connections/followers_and_following/following.json" to followingJson("alice", "carol"),
            "account/profile/personal_information.json" to "{\"name\":\"Private data\"}",
            "media/avatar.jpg" to "not an image fixture",
        )

        val result = importer.importZip(ByteArrayInputStream(archive))

        assertEquals(listOf("alice", "bob"), result.followers.map { it.username })
        assertEquals(listOf("alice", "carol"), result.following.map { it.username })
        assertEquals(3, result.sourceFiles.size)
    }

    @Test
    fun `parses official HTML export files`() {
        val archive = zipOf(
            "connections/followers_and_following/followers_1.html" to htmlPage(
                "Followers",
                "alice",
                "bob",
            ),
            "connections/followers_and_following/following.html" to htmlPage(
                "Following",
                "bob",
                "carol",
            ),
        )

        val result = importer.importZip(ByteArrayInputStream(archive))

        assertEquals(listOf("alice", "bob"), result.followers.map { it.username })
        assertEquals(listOf("bob", "carol"), result.following.map { it.username })
    }

    @Test
    fun `uses custom names and parses XML compatibility files`() {
        val archive = zipOf(
            "renamed/files/f1.xml" to xmlRelationships("alice"),
            "renamed/files/f2.xml" to xmlRelationships("alice", "dana"),
        )

        val result = importer.importZip(
            inputStream = ByteArrayInputStream(archive),
            settings = ExportFileSettings(followersFileName = "f1", followingFileName = "f2.xml"),
        )

        assertEquals(listOf("alice"), result.followers.map { it.username })
        assertEquals(listOf("alice", "dana"), result.following.map { it.username })
    }

    @Test
    fun `reports missing relationship files clearly`() {
        val archive = zipOf("profile/profile.json" to "{\"name\":\"Example\"}")

        val failure = runCatching {
            importer.importZip(ByteArrayInputStream(archive))
        }.exceptionOrNull()

        assertTrue(failure is InstagramExportException)
        assertTrue(failure?.message.orEmpty().contains("followers and following"))
    }

    @Test
    fun `rejects ambiguous custom file names`() {
        val failure = runCatching {
            importer.importZip(
                inputStream = ByteArrayInputStream(zipOf("f1.json" to followersJson("alice"))),
                settings = ExportFileSettings(followersFileName = "f1.json", followingFileName = "f1"),
            )
        }.exceptionOrNull()

        assertTrue(failure is InstagramExportException)
        assertTrue(failure?.message.orEmpty().contains("must be different"))
    }

    private fun followersJson(vararg usernames: String): String = usernames.joinToString(
        prefix = "[",
        postfix = "]",
    ) { relationshipJson(it) }

    private fun followingJson(vararg usernames: String): String = usernames.joinToString(
        prefix = "{\"relationships_following\":[",
        postfix = "]}",
    ) { relationshipJson(it) }

    private fun relationshipJson(username: String): String =
        """{"title":"$username","string_list_data":[{"href":"https://www.instagram.com/$username/","value":"$username","timestamp":1}]}"""

    private fun htmlPage(title: String, vararg usernames: String): String = buildString {
        append("<!doctype html><html><head><title>$title</title></head><body><h1>$title</h1>")
        usernames.forEach { username ->
            append("<a target=\"_blank\" href=\"https://www.instagram.com/_u/$username\">$username</a>")
        }
        append("</body></html>")
    }

    private fun xmlRelationships(vararg usernames: String): String = buildString {
        append("<?xml version=\"1.0\"?><relationships>")
        usernames.forEach { username ->
            append("<relationship><value>$username</value><href>https://www.instagram.com/$username/</href></relationship>")
        }
        append("</relationships>")
    }

    private fun zipOf(vararg entries: Pair<String, String>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (path, content) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }
}
