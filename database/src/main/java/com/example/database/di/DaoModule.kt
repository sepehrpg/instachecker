/*

package com.example.database.di

import com.example.database.RoomDb
import com.example.database.dao.NonFollowersDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaoModule {
    @Provides
    fun providesArticlesDao(
        database: RoomDb,
    ): NonFollowersDao = database.nonFollowersDao()

}
*/
