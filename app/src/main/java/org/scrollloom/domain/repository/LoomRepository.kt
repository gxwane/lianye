package org.scrollloom.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.scrollloom.domain.model.LoomAction
import org.scrollloom.domain.model.WeavingState

class LoomRepository {

    private val _weavingState = MutableStateFlow<WeavingState>(WeavingState.Idle(false))
    val weavingState: StateFlow<WeavingState> = _weavingState.asStateFlow()

    private var isConnected = false

    fun updateServiceConnected(connected: Boolean) {
        isConnected = connected
        val currentState = _weavingState.value
        if (currentState is WeavingState.Idle) {
            _weavingState.value = WeavingState.Idle(connected)
        } else if (!connected) {
            _weavingState.value = WeavingState.Idle(false)
        }
    }

    fun triggerAction(action: LoomAction) {
        when (action) {
            is LoomAction.Start -> {
                if (!isConnected) {
                    _weavingState.value = WeavingState.Error("无障碍服务尚未启用，请先开启服务")
                } else {
                    _weavingState.value = WeavingState.Capturing(stripCount = 0)
                }
            }
            is LoomAction.Stop -> {
                if (_weavingState.value is WeavingState.Capturing) {
                    _weavingState.value = WeavingState.Preview(tileCount = 0, totalHeightPx = 0)
                }
            }
            is LoomAction.Reset -> {
                _weavingState.value = WeavingState.Idle(isServiceConnected = isConnected)
            }
            is LoomAction.ReportError -> {
                _weavingState.value = WeavingState.Error(action.message)
            }
        }
    }
}
