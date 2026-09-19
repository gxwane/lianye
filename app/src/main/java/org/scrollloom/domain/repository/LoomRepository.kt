package org.scrollloom.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.scrollloom.domain.model.LoomAction
import org.scrollloom.domain.model.WeavingState
import org.scrollloom.engine.model.TileMetadata

import android.os.Build

class LoomRepository {

    private val _weavingState = MutableStateFlow<WeavingState>(WeavingState.Idle(false))
    val weavingState: StateFlow<WeavingState> = _weavingState.asStateFlow()

    private val _isProjectionGranted = MutableStateFlow(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
    val isProjectionGranted: StateFlow<Boolean> = _isProjectionGranted.asStateFlow()

    private var isConnected = false

    fun updateProjectionGranted(granted: Boolean) {
        _isProjectionGranted.value = granted
    }

    fun updateServiceConnected(connected: Boolean) {
        isConnected = connected
        val currentState = _weavingState.value
        if (currentState is WeavingState.Idle) {
            _weavingState.value = WeavingState.Idle(connected)
        } else if (!connected) {
            _weavingState.value = WeavingState.Idle(false)
        }
    }

    fun startWeaving() {
        _weavingState.value = WeavingState.Weaving(frameCount = 0, currentHeightPx = 0)
    }

    fun updateProgress(frameCount: Int, currentHeight: Int) {
        if (_weavingState.value is WeavingState.Weaving) {
            _weavingState.value = WeavingState.Weaving(frameCount, currentHeight)
        }
    }

    fun finishWeaving(tiles: List<TileMetadata>) {
        val totalHeight = tiles.sumOf { it.height }
        _weavingState.value = WeavingState.Preview(tiles, totalHeight)
    }

    fun failWeaving(message: String, partialTiles: List<TileMetadata> = emptyList()) {
        _weavingState.value = WeavingState.Error(message, partialTiles)
    }

    fun reset() {
        _weavingState.value = WeavingState.Idle(isServiceConnected = isConnected)
    }

    fun triggerAction(action: LoomAction) {
        when (action) {
            is LoomAction.Start -> {
                if (!isConnected) {
                    _weavingState.value = WeavingState.Error("无障碍服务尚未启用，请先开启服务")
                } else {
                    startWeaving()
                }
            }
            is LoomAction.Stop -> {
                val current = _weavingState.value
                if (current is WeavingState.Weaving) {
                    _weavingState.value = WeavingState.Preview(emptyList(), current.currentHeightPx)
                }
            }
            is LoomAction.Reset -> reset()
            is LoomAction.ReportError -> failWeaving(action.message)
        }
    }
}
