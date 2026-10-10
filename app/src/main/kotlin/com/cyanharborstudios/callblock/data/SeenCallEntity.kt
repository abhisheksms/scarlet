package com.cyanharborstudios.callblock.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cyanharborstudios.callblock.core.frequent.SeenCall

/**
 * One call the app was asked about while it was on, allowed or not: only the number's key
 * and the moment. The record the frequent callers are found in (ADR-010). Kept for sixty
 * days; nothing is written while the mode in effect is Off; History never shows it, and
 * Delete All in History clears it too.
 */
@Entity(tableName = "seen_calls", indices = [Index("at_millis")])
data class SeenCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "number_key") val numberKey: String,
    @ColumnInfo(name = "at_millis") val atMillis: Long,
) {
    fun toSeenCall() = SeenCall(numberKey, atMillis)
}
