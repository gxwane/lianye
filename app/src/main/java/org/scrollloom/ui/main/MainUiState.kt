package org.scrollloom.ui.main

import android.os.Build
import androidx.annotation.StringRes
import org.scrollloom.domain.model.WeavingState

enum class RestrictedCardPhase {
    HIDDEN,               // 不展示（未尝试开启、或非受限设备、或服务已连接）
    BLOCKED_GUIDE,        // 放行前：直达系统应用详情页放行
    ALLOWED_READY_RETURN  // 放行后：提示已解除限制，直达无障碍设置开启服务
}

/**
 * 纯粹 UI 状态模型，集中计算系统版本逻辑，彻底隔离平台依赖与 Compose 渲染。
 */
data class MainUiState(
    val isServiceConnected: Boolean,
    val isProjectionGranted: Boolean,
    val showMediaProjectionCard: Boolean,
    val restrictedCardPhase: RestrictedCardPhase,
    @StringRes val preFlightHintRes: Int
) {
    companion object {
        fun create(
            weavingState: WeavingState,
            isProjectionGranted: Boolean,
            hasAttemptedEnable: Boolean,
            hasEncounteredRestriction: Boolean,
            isRestrictedBlocked: Boolean,
            preFlightHintRes: Int
        ): MainUiState {
            val isConnected = (weavingState as? WeavingState.Idle)?.isServiceConnected ?: false

            val phase = when {
                isConnected -> RestrictedCardPhase.HIDDEN
                !hasAttemptedEnable -> RestrictedCardPhase.HIDDEN
                isRestrictedBlocked -> RestrictedCardPhase.BLOCKED_GUIDE
                hasEncounteredRestriction && !isRestrictedBlocked -> RestrictedCardPhase.ALLOWED_READY_RETURN
                else -> RestrictedCardPhase.HIDDEN
            }

            return MainUiState(
                isServiceConnected = isConnected,
                isProjectionGranted = isProjectionGranted,
                showMediaProjectionCard = (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) && !isProjectionGranted,
                restrictedCardPhase = phase,
                preFlightHintRes = preFlightHintRes
            )
        }
    }
}
