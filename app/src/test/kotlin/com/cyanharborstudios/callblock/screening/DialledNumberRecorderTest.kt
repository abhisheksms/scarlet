package com.cyanharborstudios.callblock.screening

import com.cyanharborstudios.callblock.core.rules.ScreeningSettings
import com.cyanharborstudios.callblock.data.DialledNumberDao
import com.cyanharborstudios.callblock.data.DialledNumberEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** What is kept of the calls the user makes, with the database replaced by a map. */
class DialledNumberRecorderTest {

    private class FakeDao : DialledNumberDao {
        val rows = mutableMapOf<String, Long>()

        override suspend fun upsert(entry: DialledNumberEntity) {
            rows[entry.numberKey] = entry.atMillis
        }

        override suspend fun lastDialledAt(numberKey: String): Long? = rows[numberKey]

        override suspend fun deleteOlderThan(beforeMillis: Long) {
            rows.entries.removeAll { it.value < beforeMillis }
        }

        override suspend fun deleteAll() = rows.clear()
    }

    private val now = 1_000_000_000_000L
    private val hour = 3_600_000L

    private fun recorder(dao: FakeDao, settings: ScreeningSettings = ScreeningSettings(), homeRegion: String = "IN") =
        DialledNumberRecorder(dao, settings = { settings }, homeRegion = { homeRegion })

    @Test
    fun `a dialled number is kept under the key an incoming call from it will have`() = runTest {
        val dao = FakeDao()
        recorder(dao).record("080 4567 8901", now)
        assertEquals(mapOf("+918045678901" to now), dao.rows)
    }

    @Test
    fun `calling a number again keeps one row, with the later time`() = runTest {
        val dao = FakeDao()
        recorder(dao).record("+918045678901", now)
        recorder(dao).record("08045678901", now + hour)
        assertEquals(mapOf("+918045678901" to now + hour), dao.rows)
    }

    @Test
    fun `nothing is kept while call backs are switched off`() = runTest {
        val dao = FakeDao()
        recorder(dao, ScreeningSettings(callBacksRing = false)).record("+918045678901", now)
        assertEquals(emptyMap<String, Long>(), dao.rows)
    }

    @Test
    fun `a call with no number is not kept`() = runTest {
        val dao = FakeDao()
        recorder(dao).record(null, now)
        recorder(dao).record("", now)
        assertEquals(emptyMap<String, Long>(), dao.rows)
    }

    @Test
    fun `a number is forgotten once it can no longer ring back`() = runTest {
        val dao = FakeDao()
        val settings = ScreeningSettings(callBackWindowMinutes = 60)
        recorder(dao, settings).record("+918045678901", now)
        // The next outgoing call, an hour and a minute later, tidies the first away.
        recorder(dao, settings).record("+918045678902", now + hour + 60_000)
        assertEquals(mapOf("+918045678902" to now + hour + 60_000), dao.rows)
        // One made within the hour is still there.
        recorder(dao, settings).record("+918045678903", now + hour + 120_000)
        assertEquals(setOf("+918045678902", "+918045678903"), dao.rows.keys)
    }
}
