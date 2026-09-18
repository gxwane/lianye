package org.scrollloom.service.gesture

import org.scrollloom.service.gesture.model.ControlledScrollCommand

interface GestureDispatcher {
    fun sendGesture(command: ControlledScrollCommand, callback: (Boolean) -> Unit): Boolean
    fun sendCancelGesture(): Boolean
}
