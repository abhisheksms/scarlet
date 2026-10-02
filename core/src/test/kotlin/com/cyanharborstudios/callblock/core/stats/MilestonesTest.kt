package com.cyanharborstudios.callblock.core.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MilestonesTest {

    @Test
    fun `before the first milestone nothing is reached yet`() {
        val progress = Milestones.progress(4)
        assertNull(progress.reached)
        assertEquals(10, progress.next)
        assertEquals(0.4f, progress.fractionToNext, 0.001f)
    }

    @Test
    fun `progress runs from the milestone reached to the next one`() {
        val progress = Milestones.progress(156)
        assertEquals(100, progress.reached)
        assertEquals(250, progress.next)
        assertEquals(56f / 150f, progress.fractionToNext, 0.001f)
    }

    @Test
    fun `landing exactly on a milestone reaches it`() {
        assertEquals(100, Milestones.progress(100).reached)
        assertEquals(0f, Milestones.progress(100).fractionToNext, 0.001f)
    }

    @Test
    fun `past the top of the ladder there is no next milestone`() {
        val progress = Milestones.progress(Milestones.LADDER.last() + 5)
        assertEquals(Milestones.LADDER.last(), progress.reached)
        assertNull(progress.next)
        assertEquals(1f, progress.fractionToNext, 0.001f)
    }

    @Test
    fun `a milestone is crossed only by the call that reaches it`() {
        assertEquals(10, Milestones.crossed(before = 9, after = 10))
        assertNull(Milestones.crossed(before = 10, after = 11))
        assertNull(Milestones.crossed(before = 8, after = 9))
        assertEquals(25, Milestones.crossed(before = 9, after = 30)) // a jump reports the highest one passed
    }
}
