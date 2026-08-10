package com.sepehrpg.scaninsta.data.repository

import com.example.database.dao.UserDao
import com.example.database.model.PageEntity
import com.example.database.model.UserEntity
import com.example.database.model.UserType
import com.sepehrpg.scaninsta.data.importer.InstagramAccount
import com.sepehrpg.scaninsta.data.importer.InstagramExportData
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao) {
    val allPages: Flow<List<PageEntity>> = userDao.getAllPages()
    val latestPageId: Flow<Int?> = userDao.getLatestPageId()

    fun getUnfollowersForPage(pageId: Int): Flow<List<UserEntity>> {
        return userDao.getUnfollowersForPage(pageId)
    }

    suspend fun analyzeAndStoreUserData(export: InstagramExportData, pageName: String): Int {
        val followersEntities = export.followers.map { it.toUserEntity(UserType.FOLLOWER) }
        val followingEntities = export.following.map { it.toUserEntity(UserType.FOLLOWING) }
        val followersUsernames = followersEntities
            .map { it.username.lowercase(Locale.ROOT) }
            .toSet()
        val unfollowersEntities = followingEntities
            .filter { it.username.lowercase(Locale.ROOT) !in followersUsernames }
            .map { it.copy(id = 0, userType = UserType.UNFOLLOWER) }

        return userDao.insertAnalysis(
            page = PageEntity(name = pageName),
            users = followersEntities + followingEntities + unfollowersEntities,
        ).toInt()
    }

    suspend fun deletePage(pageId: Int) {
        userDao.deletePageById(pageId)
    }
}

private fun InstagramAccount.toUserEntity(type: UserType): UserEntity {
    return UserEntity(
        username = username,
        href = profileUrl,
        userType = type,
        pageId = 0,
    )
}
