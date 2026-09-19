package org.scrollloom.engine.model

data class MatchResult(
    val deltaY: Int,
    val sadScore: Float,
    val ambiguityRatio: Float,
    val isBottomReached: Boolean,
    val isSecureBlocked: Boolean = false,
    val isStationary: Boolean = false,
    val isDynamicScene: Boolean = false
)
