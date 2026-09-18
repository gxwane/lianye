package org.scrollloom.domain.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.scrollloom.domain.model.WeavingState
import org.scrollloom.engine.model.TileMetadata
import java.io.File

class LoomRepositoryTest {

    private lateinit var repository: LoomRepository

    @Before
    fun setUp() {
        repository = LoomRepository()
    }

    @Test
    fun `initial state should be Idle with disconnected service`() {
        assertEquals(WeavingState.Idle(false), repository.weavingState.value)
    }

    @Test
    fun `startWeaving should transition state to Weaving with zero frames`() {
        repository.updateServiceConnected(true)
        repository.startWeaving()
        val state = repository.weavingState.value
        assertTrue(state is WeavingState.Weaving)
        val weaving = state as WeavingState.Weaving
        assertEquals(0, weaving.frameCount)
        assertEquals(0, weaving.currentHeightPx)
    }

    @Test
    fun `updateProgress should update frameCount and height`() {
        repository.startWeaving()
        repository.updateProgress(4, 1500)
        val state = repository.weavingState.value as WeavingState.Weaving
        assertEquals(4, state.frameCount)
        assertEquals(1500, state.currentHeightPx)
    }

    @Test
    fun `finishWeaving should transition to Preview with correct totalHeight`() {
        repository.startWeaving()
        val tile1 = TileMetadata(0, File("t0"), 100, 500, 0)
        val tile2 = TileMetadata(1, File("t1"), 100, 300, 500)

        repository.finishWeaving(listOf(tile1, tile2))

        val state = repository.weavingState.value
        assertTrue(state is WeavingState.Preview)
        val preview = state as WeavingState.Preview
        assertEquals(2, preview.tiles.size)
        assertEquals(800, preview.totalHeightPx)
    }

    @Test
    fun `failWeaving should transition to Error preserving partial tiles`() {
        repository.startWeaving()
        val partialTile = TileMetadata(0, File("p0"), 100, 400, 0)

        repository.failWeaving("Secure blocked", listOf(partialTile))

        val state = repository.weavingState.value
        assertTrue(state is WeavingState.Error)
        val error = state as WeavingState.Error
        assertEquals("Secure blocked", error.message)
        assertEquals(1, error.partialTiles.size)
    }

    @Test
    fun `reset should return to Idle`() {
        repository.updateServiceConnected(true)
        repository.startWeaving()
        repository.reset()
        assertEquals(WeavingState.Idle(true), repository.weavingState.value)
    }
}
