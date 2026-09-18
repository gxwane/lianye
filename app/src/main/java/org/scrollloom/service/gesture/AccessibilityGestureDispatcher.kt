package org.scrollloom.service.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import org.scrollloom.service.gesture.model.ControlledScrollCommand

class AccessibilityGestureDispatcher(
    private val service: AccessibilityService
) : GestureDispatcher {

    override fun sendGesture(command: ControlledScrollCommand, callback: (Boolean) -> Unit): Boolean {
        val movePath = Path().apply {
            moveTo(command.startX, command.startY)
            lineTo(command.endX, command.endY)
        }
        val moveStroke = GestureDescription.StrokeDescription(
            movePath, 0L, command.moveDurationMs, true
        )

        val holdPath = Path().apply {
            moveTo(command.endX, command.endY)
            lineTo(command.endX, command.endY)
        }
        val holdStroke = moveStroke.continueStroke(
            holdPath, 0L, command.holdDurationMs, false
        )

        val gesture = GestureDescription.Builder()
            .addStroke(moveStroke)
            .addStroke(holdStroke)
            .build()

        return service.dispatchGesture(
            gesture,
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    callback(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    callback(false)
                }
            },
            null
        )
    }

    override fun sendCancelGesture(): Boolean {
        val cancelPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, 0f)
        }
        val cancelStroke = GestureDescription.StrokeDescription(cancelPath, 0L, 1L)
        val cancelGesture = GestureDescription.Builder().addStroke(cancelStroke).build()
        return service.dispatchGesture(cancelGesture, null, null)
    }
}
