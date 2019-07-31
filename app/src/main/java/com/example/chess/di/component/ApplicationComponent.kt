package com.example.chess.di.component

import android.content.Context
import com.example.chess.di.module.ApplicationModule
import com.example.chess.network.INetworkService
import dagger.Component
import javax.inject.Singleton

/**
 * @author v.peschaniy
 *      Date: 17.07.2019
 */
@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    var context: Context

    var networkService: INetworkService
}