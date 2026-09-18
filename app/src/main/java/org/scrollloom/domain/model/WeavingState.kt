package org.scrollloom.domain.model

import org.scrollloom.engine.model.TileMetadata

sealed interface WeavingState {
    data class Idle(val isServiceConnected: Boolean = false) : WeavingState
    data class Weaving(val frameCount: Int = 0, val currentHeightPx: Int = 0) : WeavingState
    data class Preview(val tiles: List<TileMetadata> = emptyList(), val totalHeightPx: Int = 0) : WeavingState
    data class Error(val message: String, val partialTiles: List<TileMetadata> = emptyList()) : WeavingState
}

sealed interface LoomAction {
    data object Start : LoomAction
    data object Stop : LoomAction
    data object Reset : LoomAction
    data class ReportError(val message: String) : LoomAction
}
