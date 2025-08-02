package com.example.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


enum class UserType {
    FOLLOWER,
    FOLLOWING,
    UNFOLLOWER
}

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = PageEntity::class,
            parentColumns = ["id"],
            childColumns = ["pageId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val href: String,
    val userType: UserType,
    val pageId: Int
)

