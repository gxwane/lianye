package org.scrollloom.service.gesture

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.scrollloom.service.gesture.model.ControlledScrollCommand
import kotlin.coroutines.resume

class AntiFlingController(
    private val dispatcher: GestureDispatcher,
    private val timeoutMs: Long = 1500L
) {
    suspend fun executeControlledScroll(command: ControlledScrollCommand): Boolean {
        val result = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                // P0 Guard: Ensure touch queue is released upon timeout or parent cancellation
                continuation.invokeOnCancellation {
                    dispatcher.sendCancelGesture()
                }

                val dispatched = dispatcher.sendGesture(command) { success ->
                    if (continuation.isActive) {
                        continuation.resume(success)
                    }
                }

                if (!dispatched && continuation.isActive) {
                    continuation.resume(false)
                }
            }
        }

        return if (result == null) {
            // Watchdog timeout recovery
            dispatcher.sendCancelGesture()
            false
        } else {
            result
        }
    }

    fun cancelCurrentGesture(): Boolean = dispatcher.sendCancelGesture()
}
