package com.example.chess.di.module

import android.content.Context
import com.example.chess.di.App
import com.example.chess.network.INetworkService
import com.example.chess.network.NetworkService
import dagger.Module
import dagger.Provides
/**
 * @author v.peschaniy
 *      Date: 18.07.2019
 */
@Module
class ApplicationModule constructor(private val application: App) {

    @Provides
    fun provideContext(): Context = application

    @Provides
    fun provideNetworkService(networkService: NetworkService): INetworkService = networkService

}