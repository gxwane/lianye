package org.scrollloom.domain.model

sealed interface WeavingState {
    data class Idle(val isServiceConnected: Boolean = false) : WeavingState
    data class Capturing(val stripCount: Int = 0) : WeavingState
    data class Weaving(val progress: Float = 0f) : WeavingState
    data class Preview(val tileCount: Int = 0, val totalHeightPx: Int = 0) : WeavingState
    data class Error(val message: String) : WeavingState
}

sealed interface LoomAction {
    data object Start : LoomAction
    data object Stop : LoomAction
    data object Reset : LoomAction
    data class ReportError(val message: String) : LoomAction
}
