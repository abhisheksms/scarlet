package com.cyanharborstudios.callblock.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HandledCallEntity::class, AllowedNumberEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun handledCalls(): HandledCallDao

    abstract fun allowedNumbers(): AllowedNumberDao

    companion object {
        fun open(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "callblock.db").build()
    }
}
