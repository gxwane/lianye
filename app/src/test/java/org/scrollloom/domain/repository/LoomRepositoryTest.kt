package org.scrollloom.domain.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.scrollloom.domain.model.LoomAction
import org.scrollloom.domain.model.WeavingState

@OptIn(ExperimentalCoroutinesApi::class)
class LoomRepositoryTest {

    private lateinit var repository: LoomRepository

    @Before
    fun setUp() {
        repository = LoomRepository()
    }

    @Test
    fun `initial state should be Idle with service disconnected`() = runTest {
        val state = repository.weavingState.first()
        assertTrue("State should be Idle", state is WeavingState.Idle)
        assertFalse("Service should not be connected initially", (state as WeavingState.Idle).isServiceConnected)
    }

    @Test
    fun `updateServiceConnected should reflect connection status correctly`() = runTest {
        repository.updateServiceConnected(true)
        val connectedState = repository.weavingState.first()
        assertTrue("State should be Idle", connectedState is WeavingState.Idle)
        assertTrue("Service should be connected", (connectedState as WeavingState.Idle).isServiceConnected)

        repository.updateServiceConnected(false)
        val disconnectedState = repository.weavingState.first()
        assertFalse("Service should be disconnected", (disconnectedState as WeavingState.Idle).isServiceConnected)
    }

    @Test
    fun `triggerAction Start should transition to Capturing when service is connected`() = runTest {
        repository.updateServiceConnected(true)
        repository.triggerAction(LoomAction.Start)

        val state = repository.weavingState.first()
        assertTrue("State should be Capturing", state is WeavingState.Capturing)
        assertEquals("Strip count should start at 0", 0, (state as WeavingState.Capturing).stripCount)
    }

    @Test
    fun `triggerAction Start should fail with error when service is not connected`() = runTest {
        repository.updateServiceConnected(false)
        repository.triggerAction(LoomAction.Start)

        val state = repository.weavingState.first()
        assertTrue("State should be Error when starting without service", state is WeavingState.Error)
    }

    @Test
    fun `triggerAction Stop should transition from Capturing to Preview`() = runTest {
        repository.updateServiceConnected(true)
        repository.triggerAction(LoomAction.Start)
        repository.triggerAction(LoomAction.Stop)

        val state = repository.weavingState.first()
        assertTrue("State should be Preview after stopping capture", state is WeavingState.Preview)
    }

    @Test
    fun `triggerAction Reset should return to Idle`() = runTest {
        repository.updateServiceConnected(true)
        repository.triggerAction(LoomAction.Start)
        repository.triggerAction(LoomAction.Reset)

        val state = repository.weavingState.first()
        assertTrue("State should be Idle after reset", state is WeavingState.Idle)
        assertTrue("Service should remain connected after reset", (state as WeavingState.Idle).isServiceConnected)
    }
}
