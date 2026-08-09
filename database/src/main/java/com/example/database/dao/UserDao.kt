package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.database.model.PageEntity
import com.example.database.model.UserEntity
import com.example.database.model.UserWithPage
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: PageEntity): Long

    /** Stores one complete analysis atomically so a failed import cannot leave an empty page. */
    @Transaction
    suspend fun insertAnalysis(page: PageEntity, users: List<UserEntity>): Long {
        val pageId = insertPage(page)
        insertUsers(users.map { it.copy(pageId = pageId.toInt()) })
        return pageId
    }

    @Query("SELECT * FROM users WHERE userType = 'UNFOLLOWER' AND pageId = :pageId")
    fun getUnfollowersForPage(pageId: Int): Flow<List<UserEntity>>

    @Query("SELECT MAX(id) FROM page")
    fun getLatestPageId(): Flow<Int?>

    @Query("SELECT * FROM page ORDER BY id DESC")
    fun getAllPages(): Flow<List<PageEntity>>

    @Query("DELETE FROM page WHERE id = :pageId")
    suspend fun deletePageById(pageId: Int)

    @Query("DELETE FROM page")
    suspend fun clearAllPages()

    @Transaction
    @Query("SELECT * FROM users")
    suspend fun getUsersWithPage(): List<UserWithPage>
}
