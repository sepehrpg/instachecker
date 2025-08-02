package com.example.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "page")
data class PageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)