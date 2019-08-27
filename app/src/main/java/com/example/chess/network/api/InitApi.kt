package com.example.chess.network.api

import com.example.chess.shared.dto.GameDTO
import com.example.chess.shared.enums.GameMode
import com.example.chess.shared.enums.Side
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author v.peschaniy
 * Date: 16.07.2019
 */
interface InitApi {

    @GET("debug/version")
    fun getVersion(): Call<String>

    @GET("init/new")
    fun createGame(): Call<GameDTO>

    @GET("init/continue")
    fun getGame(@Query("userId") userId: String, @Query("gameId") gameId: Long): Call<GameDTO>

    @POST("init/mode")
    fun setGameMode(
        @Query("userId") userId: String,
        @Query("gameId") gameId: Long,
        @Query("mode") mode: GameMode
    ): Call<GameDTO>

    @POST("init/side")
    fun setSide(
        @Query("userId") userId: String,
        @Query("gameId") gameId: Long,
        @Query("side") side: Side
    ): Call<GameDTO>
}
