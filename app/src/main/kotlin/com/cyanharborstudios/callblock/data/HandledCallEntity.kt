package com.cyanharborstudios.callblock.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cyanharborstudios.callblock.core.rules.Action
import com.cyanharborstudios.callblock.core.stats.HandledCall

/** One call the app blocked or silenced. The app's own log; the system call log is never read. */
@Entity(
    tableName = "handled_calls",
    indices = [Index("at_millis"), Index("number_key")],
)
data class HandledCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** The number exactly as Android reported it. Empty if the call carried none. */
    @ColumnInfo(name = "number_raw") val numberRaw: String,
    /** The number's key (see PhoneNumber.key), for matching. */
    @ColumnInfo(name = "number_key") val numberKey: String,
    @ColumnInfo(name = "at_millis") val atMillis: Long,
    /** "BLOCK" or "SILENCE". */
    val action: String,
    /** The id of the rule that decided this call. */
    @ColumnInfo(name = "rule_id") val ruleId: String,
) {
    fun toHandledCall() = HandledCall(
        numberKey = numberKey,
        numberRaw = numberRaw,
        atMillis = atMillis,
        action = if (action == Action.SILENCE.name) Action.SILENCE else Action.BLOCK,
    )
}
