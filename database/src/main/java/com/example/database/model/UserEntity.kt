package com.example.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey



enum class UserType {
    FOLLOWER,
    FOLLOWING,
    UNFOLLOWER
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val href: String,
    val userType: UserType
)

