package com.sepehrpg.scaninsta.data.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



@Serializable
data class FollowingWrapper(
    @SerialName("relationships_following")
    val relationshipsFollowing: List<InstagramUserData>
)

@Serializable
data class InstagramUserData(
    val title: String? = null,
    @SerialName("string_list_data")
    val instagramUserInfo: List<InstagramUserInfo> = emptyList()
)

@Serializable
data class InstagramUserInfo(
    val href: String,
    val value: String,
    val timestamp: Long,
)