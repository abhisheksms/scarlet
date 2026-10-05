package com.cyanharborstudios.callblock.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HandledCallEntity::class, AllowedNumberEntity::class, DialledNumberEntity::class],
    // 2 (5 October 2026) adds dialled_numbers. Room writes the step from the two exported schemas.
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun handledCalls(): HandledCallDao

    abstract fun allowedNumbers(): AllowedNumberDao

    abstract fun dialledNumbers(): DialledNumberDao

    companion object {
        fun open(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "callblock.db").build()
    }
}
