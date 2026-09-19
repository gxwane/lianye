package org.scrollloom

import android.app.Application
import org.scrollloom.di.AppComponent

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import org.scrollloom.service.capture.LoomMediaProjectionService

class App : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        appComponent = AppComponent()
        configureComponentIsolation()
    }

    private fun configureComponentIsolation() {
        // Enforce Android 14+ FGS isolation:
        // On API 30+, LoomMediaProjectionService is completely disabled.
        // It is only enabled and active on Android 10 (API 29).
        val component = ComponentName(this, LoomMediaProjectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {
            packageManager.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
