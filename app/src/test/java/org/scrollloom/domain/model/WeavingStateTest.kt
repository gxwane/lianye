package org.scrollloom.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeavingStateTest {

    @Test
    fun `test state hierarchy and properties`() {
        val idle = WeavingState.Idle(isServiceConnected = true)
        assertTrue(idle.isServiceConnected)

        val capturing = WeavingState.Capturing(stripCount = 5)
        assertEquals(5, capturing.stripCount)

        val weaving = WeavingState.Weaving(progress = 0.75f)
        assertEquals(0.75f, weaving.progress, 0.001f)

        val preview = WeavingState.Preview(tileCount = 3, totalHeightPx = 6000)
        assertEquals(3, preview.tileCount)
        assertEquals(6000, preview.totalHeightPx)

        val error = WeavingState.Error("Security window blocked")
        assertEquals("Security window blocked", error.message)
    }
}
