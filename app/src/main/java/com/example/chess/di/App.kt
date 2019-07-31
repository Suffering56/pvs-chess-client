package com.example.chess.di

import android.app.Application
import android.content.Context
import com.example.chess.di.component.ApplicationComponent
import com.example.chess.di.component.DaggerApplicationComponent
import com.example.chess.di.module.ApplicationModule

/**
 * @author v.peschaniy
 *      Date: 17.07.2019
 */
class App : Application() {

    lateinit var applicationComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()

        applicationComponent.context = this
    }

    companion object {
        fun get(context: Context): App {
            return context.applicationContext as App
        }
    }
}