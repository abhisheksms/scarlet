package com.cyanharborstudios.callblock.screening

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Which outgoing calls pause the app, with Android's answer and the settings replaced by fakes. */
class EmergencyCallPauseTest {

    private val now = 1_000_000_000_000L
    private val pausedAt = mutableListOf<Long>()
    private val asked = mutableListOf<String>()

    private fun watch(emergencyNumbers: Set<String> = setOf("112")) = EmergencyCallPause(
        isEmergencyNumber = { number ->
            asked += number
            number in emergencyNumbers
        },
        pause = { atMillis -> pausedAt += atMillis },
    )

    @Test
    fun `a call to an emergency number pauses the app from that moment`() = runTest {
        assertTrue(watch().onOutgoingCall("112", now))
        assertEquals(listOf(now), pausedAt)
    }

    @Test
    fun `any other call changes nothing`() = runTest {
        assertFalse(watch().onOutgoingCall("+918045678901", now))
        assertEquals(emptyList<Long>(), pausedAt)
    }

    @Test
    fun `a call with no number is not looked up and changes nothing`() = runTest {
        val watch = watch()
        assertFalse(watch.onOutgoingCall(null, now))
        assertFalse(watch.onOutgoingCall("  ", now))
        assertEquals(emptyList<String>(), asked)
        assertEquals(emptyList<Long>(), pausedAt)
    }
}
