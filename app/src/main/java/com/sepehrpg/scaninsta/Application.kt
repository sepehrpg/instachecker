package com.sepehrpg.scaninsta

import android.app.Application
import com.example.database.AppDatabase
import com.sepehrpg.scaninsta.data.repository.UserRepository

class Application : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { UserRepository(database.userDao()) }
}