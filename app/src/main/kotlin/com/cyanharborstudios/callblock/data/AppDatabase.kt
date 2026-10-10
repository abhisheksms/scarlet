package com.cyanharborstudios.callblock.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HandledCallEntity::class, AllowedNumberEntity::class, DialledNumberEntity::class, SeenCallEntity::class],
    // 2 (5 October 2026) adds dialled_numbers; 3 (10 October 2026) adds seen_calls. Room writes each step from the exported schemas.
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun handledCalls(): HandledCallDao

    abstract fun allowedNumbers(): AllowedNumberDao

    abstract fun dialledNumbers(): DialledNumberDao

    abstract fun seenCalls(): SeenCallDao

    companion object {
        fun open(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "callblock.db").build()
    }
}
