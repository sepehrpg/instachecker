package com.example.database

import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


internal object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Step 1: Create the new 'page' table. This is correct.
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `page` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)"
            )

            // Step 2: Create a new 'users' table with the correct schema, including the foreign key.
            // We name it '_users_new' temporarily.
            db.execSQL(
                "CREATE TABLE `_users_new` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`username` TEXT NOT NULL, " +
                        "`href` TEXT NOT NULL, " +
                        "`userType` TEXT NOT NULL, " + // Enums are stored as TEXT
                        "`pageId` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`pageId`) REFERENCES `page`(`id`) ON DELETE CASCADE)"
            )

            // Step 3: Copy the data from the old 'users' table to the new one.
            // We set a default pageId (e.g., 0) for existing users, although it won't reference a real page.
            // This is necessary because the new column is NOT NULL.
            db.execSQL(
                "INSERT INTO `_users_new` (`id`, `username`, `href`, `userType`, `pageId`) " +
                        "SELECT `id`, `username`, `href`, `userType`, 0 FROM `users`"
            )

            // Step 4: Drop the old 'users' table.
            db.execSQL("DROP TABLE `users`")

            // Step 5: Rename the new table to the original name.
            db.execSQL("ALTER TABLE `_users_new` RENAME TO `users`")
        }
    }
}
