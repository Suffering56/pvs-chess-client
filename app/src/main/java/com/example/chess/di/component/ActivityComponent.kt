package com.example.chess.di.component

import com.example.chess.ChessboardActivity
import com.example.chess.MainActivity
import com.example.chess.di.ActivityScope
import com.example.chess.di.module.ActivityModule
import dagger.Component

/**
 * @author v.peschaniy
 *      Date: 18.07.2019
 */
@ActivityScope
@Component(dependencies = [ApplicationComponent::class], modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(activity: MainActivity)

    fun inject(activity: ChessboardActivity)
}