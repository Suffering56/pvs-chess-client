package com.example.chess

import retrofit2.Call
import retrofit2.http.GET

/**
 * @author v.peschaniy
 * Date: 16.07.2019
 */
interface ServerApi {

    @GET("version")
    fun getVersion(): Call<String>

}
