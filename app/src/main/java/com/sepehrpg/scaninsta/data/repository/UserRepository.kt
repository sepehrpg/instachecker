package com.sepehrpg.scaninsta.data.repository

import com.example.database.dao.UserDao
import com.example.database.model.PageEntity
import com.example.database.model.UserEntity
import com.example.database.model.UserType
import com.sepehrpg.scaninsta.data.model.FollowingWrapper
import com.sepehrpg.scaninsta.data.model.InstagramUserData
import com.sepehrpg.scaninsta.data.model.InstagramUserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao) {

    private val json = Json { ignoreUnknownKeys = true }


    val allPages: Flow<List<PageEntity>> = userDao.getAllPages()

    fun getUnfollowersForPage(pageId: Int): Flow<List<UserEntity>> {
        return userDao.getUnfollowersForPage(pageId)
    }

    val latestPageId: Flow<Int?> = userDao.getLatestPageId()


    suspend fun analyzeAndStoreUserData(followersJson: String, followingJson: String, pageName: String): Int {
        val newPage = PageEntity(name = pageName)
        val pageId = userDao.insertPage(newPage)

        val followersList = json.decodeFromString<List<InstagramUserData>>(followersJson)
        val followersEntities = followersList.mapNotNull { it.instagramUserInfo.firstOrNull() }
            .map { it.toUserEntity(UserType.FOLLOWER, pageId.toInt()) }
        userDao.insertUsers(followersEntities)

        val followingWrapper = json.decodeFromString<FollowingWrapper>(followingJson)
        val followingEntities = followingWrapper.relationshipsFollowing
            .mapNotNull { it.instagramUserInfo.firstOrNull() }
            .map { it.toUserEntity(UserType.FOLLOWING, pageId.toInt()) }
        userDao.insertUsers(followingEntities)

        val followersUsernames = followersEntities.map { it.username }.toSet()
        val unfollowersEntities = followingEntities
            .filter { it.username !in followersUsernames }
            .map { it.copy(id = 0, userType = UserType.UNFOLLOWER) }
        userDao.insertUsers(unfollowersEntities)

        return pageId.toInt()
    }

    suspend fun deletePage(pageId: Int) {
        userDao.deletePageById(pageId)
    }
}

private fun InstagramUserInfo.toUserEntity(type: UserType, pageId: Int): UserEntity {
    return UserEntity(
        username = this.value,
        href = this.href,
        userType = type,
        pageId = pageId
    )
}