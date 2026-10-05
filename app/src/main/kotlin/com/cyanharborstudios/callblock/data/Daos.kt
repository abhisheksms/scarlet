package com.cyanharborstudios.callblock.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HandledCallDao {

    @Insert
    suspend fun insert(call: HandledCallEntity): Long

    @Query("SELECT * FROM handled_calls ORDER BY at_millis DESC, id DESC")
    fun observeAll(): Flow<List<HandledCallEntity>>

    @Query("SELECT * FROM handled_calls ORDER BY at_millis DESC, id DESC")
    suspend fun all(): List<HandledCallEntity>

    @Query("SELECT MAX(at_millis) FROM handled_calls WHERE number_key = :numberKey")
    suspend fun lastHandledAt(numberKey: String): Long?

    @Query("SELECT COUNT(*) FROM handled_calls")
    suspend fun count(): Int

    @Query("DELETE FROM handled_calls WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM handled_calls")
    suspend fun deleteAll()
}

@Dao
interface DialledNumberDao {

    /** One row a number: a later call replaces the earlier time. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DialledNumberEntity)

    @Query("SELECT at_millis FROM dialled_numbers WHERE number_key = :numberKey")
    suspend fun lastDialledAt(numberKey: String): Long?

    @Query("DELETE FROM dialled_numbers WHERE at_millis < :beforeMillis")
    suspend fun deleteOlderThan(beforeMillis: Long)

    @Query("DELETE FROM dialled_numbers")
    suspend fun deleteAll()
}

@Dao
interface AllowedNumberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: AllowedNumberEntity)

    @Query("SELECT * FROM allowed_numbers ORDER BY added_at_millis DESC")
    fun observeAll(): Flow<List<AllowedNumberEntity>>

    @Query("SELECT * FROM allowed_numbers")
    suspend fun all(): List<AllowedNumberEntity>

    @Query("DELETE FROM allowed_numbers WHERE number_key = :numberKey")
    suspend fun delete(numberKey: String)

    @Query("DELETE FROM allowed_numbers WHERE expires_at_millis IS NOT NULL AND expires_at_millis <= :nowMillis")
    suspend fun deleteExpired(nowMillis: Long)
}
