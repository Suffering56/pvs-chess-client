package com.example.chess.di.module

import android.app.Activity
import dagger.Module
import dagger.Provides

/**
 * @author v.peschaniy
 *      Date: 18.07.2019
 */
@Module
class ActivityModule constructor(private val activity: Activity) {

    @Provides
    fun provideActivity(): Activity = activity
}