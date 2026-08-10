package com.sepehrpg.scaninsta.data.importer

/** File names used when locating relationship data inside an Instagram ZIP export. */
data class ExportFileSettings(
    val followersFileName: String = DEFAULT_FOLLOWERS_FILE_NAME,
    val followingFileName: String = DEFAULT_FOLLOWING_FILE_NAME,
) {
    companion object {
        const val DEFAULT_FOLLOWERS_FILE_NAME = "followers_1"
        const val DEFAULT_FOLLOWING_FILE_NAME = "following"
    }
}

/** A single Instagram account read from an exported relationship file. */
data class InstagramAccount(
    val username: String,
    val profileUrl: String,
)

/** Parsed relationship data and the archive entries from which it was loaded. */
data class InstagramExportData(
    val followers: List<InstagramAccount>,
    val following: List<InstagramAccount>,
    val sourceFiles: List<String>,
)

class InstagramExportException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
