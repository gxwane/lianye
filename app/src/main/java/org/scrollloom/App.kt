package org.scrollloom

import android.app.Application
import org.scrollloom.di.AppComponent

class App : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        appComponent = AppComponent()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
