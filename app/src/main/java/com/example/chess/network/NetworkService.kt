package com.example.chess.network

import android.content.Context
import com.example.chess.R
import com.example.chess.network.api.DebugApi
import com.example.chess.network.api.InitApi
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author v.peschaniy
 *      Date: 18.07.2019
 */
@Singleton
class NetworkService @Inject constructor(context: Context) : INetworkService {

    private val retrofit: Retrofit

    override val debugApi: DebugApi
    override val initApi: InitApi

    init {
        val mapper = jacksonObjectMapper().registerKotlinModule()
        retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.server_api_address))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(mapper))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
    }

    init {
        debugApi = retrofit.create(DebugApi::class.java)
        initApi = retrofit.create(InitApi::class.java)
    }
}
