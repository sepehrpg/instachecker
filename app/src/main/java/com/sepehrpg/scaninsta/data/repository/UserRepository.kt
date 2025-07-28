package com.sepehrpg.scaninsta.data.repository


import com.example.database.dao.UserDao
import com.example.database.model.UserEntity
import com.example.database.model.UserType
import com.sepehrpg.scaninsta.data.model.FollowingWrapper
import com.sepehrpg.scaninsta.data.model.InstagramUserData
import com.sepehrpg.scaninsta.data.model.InstagramUserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject


class UserRepository @Inject constructor (private val userDao: UserDao) {

    private val json = Json { ignoreUnknownKeys = true }

    val unfollowers: Flow<List<UserEntity>> = userDao.getUnfollowers()

    suspend fun analyzeAndStoreUserData(followersJson: String, followingJson: String) {
        userDao.clearAll()

        val followersList = json.decodeFromString<List<InstagramUserData>>(followersJson)
        val followersEntities = followersList.mapNotNull { it.instagramUserInfo.firstOrNull() }
            .map { it.toUserEntity(UserType.FOLLOWER) }
        userDao.insertUsers(followersEntities)

        val followingWrapper = json.decodeFromString<FollowingWrapper>(followingJson)
        val followingEntities = followingWrapper.relationshipsFollowing
            .mapNotNull { it.instagramUserInfo.firstOrNull() }
            .map { it.toUserEntity(UserType.FOLLOWING) }
        userDao.insertUsers(followingEntities)

        val followersUsernames = followersEntities.map { it.username }.toSet()
        val unfollowersEntities = followingEntities
            .filter { it.username !in followersUsernames }
            .map { it.copy(userType = UserType.UNFOLLOWER) }
        userDao.insertUsers(unfollowersEntities)
    }

    suspend fun clearAllData() {
        userDao.clearAll()
    }
}


private fun InstagramUserInfo.toUserEntity(type: UserType): UserEntity {
    return UserEntity(username = this.value, href = this.href, userType = type)
}