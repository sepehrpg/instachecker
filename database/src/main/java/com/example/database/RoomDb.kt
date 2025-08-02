package com.example.database
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.database.dao.UserDao
import com.example.database.model.PageEntity
import com.example.database.model.UserEntity


@Database(
    entities = [UserEntity::class, PageEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class RoomDb : RoomDatabase() {
    abstract fun userDao(): UserDao
}