package com.example.chess

import android.app.Application
import com.example.chess.di.component.ApplicationComponent
import com.example.chess.di.component.DaggerApplicationComponent
import com.example.chess.di.module.ApplicationModule

/**
 * @author v.peschaniy
 *      Date: 17.07.2019
 */
class App : Application() {

    lateinit var applicationComponent: ApplicationComponent
        private set

    override fun onCreate() {
        super.onCreate()

        applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .build()
        applicationComponent.context = this
    }
}

fun printErr(msg: String, e: Throwable? = null) {
    System.err.println(msg)
    e?.printStackTrace()
}

fun printErr(e: Throwable) {
    printErr("failure.message = ${e.message}", e)
}



