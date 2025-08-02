package com.example.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithPage(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "pageId",
        entityColumn = "id"
    )
    val page: PageEntity
)