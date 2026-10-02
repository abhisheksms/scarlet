package com.cyanharborstudios.callblock.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** A number the user has said should always ring, for good or until [expiresAtMillis]. */
@Entity(tableName = "allowed_numbers")
data class AllowedNumberEntity(
    @PrimaryKey @ColumnInfo(name = "number_key") val numberKey: String,
    @ColumnInfo(name = "number_raw") val numberRaw: String,
    @ColumnInfo(name = "added_at_millis") val addedAtMillis: Long,
    /** Null for an entry that never ends. */
    @ColumnInfo(name = "expires_at_millis") val expiresAtMillis: Long?,
)
