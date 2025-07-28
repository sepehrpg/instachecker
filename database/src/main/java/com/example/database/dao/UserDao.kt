package com.example.database.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM users WHERE userType = :userType")
    suspend fun deleteUsersByType(userType: String)

    @Query("SELECT * FROM users WHERE userType = 'UNFOLLOWER' ORDER BY username ASC")
    fun getUnfollowers(): Flow<List<UserEntity>>

    @Query("DELETE FROM users")
    suspend fun clearAll()
}