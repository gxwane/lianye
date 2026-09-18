package org.scrollloom.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import org.scrollloom.App

class LoomAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "LoomAccessibilityService connected")
        App.instance.appComponent.loomRepository.updateServiceConnected(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // STRICT INVARIANT: Blind service does not process any window events
    }

    override fun onInterrupt() {
        Log.w(TAG, "LoomAccessibilityService interrupted")
        App.instance.appComponent.loomRepository.updateServiceConnected(false)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "LoomAccessibilityService unbind")
        App.instance.appComponent.loomRepository.updateServiceConnected(false)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "LoomAccessibilityService destroyed")
        App.instance.appComponent.loomRepository.updateServiceConnected(false)
    }

    companion object {
        private const val TAG = "LoomAccessibility"
    }
}
