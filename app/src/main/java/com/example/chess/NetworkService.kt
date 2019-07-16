package com.example.chess

import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 * @author v.peschaniy
 *      Date: 16.07.2019
 */
object NetworkService {

    private const val BASE_URL = "http://172.18.64.170:8080/api/"   //"https://jsonplaceholder.typicode.com"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create())
        .build()

    fun getServerApi(): ServerApi = retrofit.create(ServerApi::class.java)
}