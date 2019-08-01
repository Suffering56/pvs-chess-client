package com.example.chess.ui

import android.app.Activity
import android.os.Bundle
import com.example.chess.App
import com.example.chess.R
import com.example.chess.di.component.ActivityComponent
import com.example.chess.di.component.DaggerActivityComponent
import com.example.chess.di.module.ActivityModule

/**
 * @author v.peschaniy
 *      Date: 31.07.2019
 */
abstract class BaseActivity : Activity() {

    protected lateinit var activityComponent: ActivityComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityComponent = DaggerActivityComponent.builder()
            .activityModule(ActivityModule(this))
            .applicationComponent((applicationContext as App).applicationComponent)
            .build()
    }
}