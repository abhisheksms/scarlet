package com.cyanharborstudios.callblock.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A number the user called that is not in their contacts, and when. Kept for a few days,
 * on the phone only, so that the number can ring when it calls back (the "you-called" rule).
 * Android shows a screening app these outgoing calls; no permission is involved.
 */
@Entity(tableName = "dialled_numbers")
data class DialledNumberEntity(
    @PrimaryKey @ColumnInfo(name = "number_key") val numberKey: String,
    @ColumnInfo(name = "at_millis") val atMillis: Long,
)
