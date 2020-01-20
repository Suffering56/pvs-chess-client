package com.example.chess.network.api

import com.example.chess.shared.dto.ChessboardDTO
import com.example.chess.shared.dto.ConstructorGameDTO
import com.example.chess.shared.dto.GameDTO
import com.example.chess.shared.enums.GameMode
import com.example.chess.shared.enums.Side
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author v.peschaniy
 * Date: 16.07.2019
 */
interface InitApi {

    @GET("init/new")
    fun createGame(
        @Query("userId") userId: String,
        @Query("mode") mode: GameMode,
        @Query("side") side: Side
    ): Call<GameDTO>

    @POST("init/constructor/new")
    fun createConstructorGame(
        @Query("userId") userId: String,
        @Query("mode") mode: GameMode,
        @Query("side") side: Side,
        @Body chessboard: ChessboardDTO
    ): Call<ConstructorGameDTO>

    @POST("init/register")
    fun registerUser(
        @Query("gameId") gameId: Long,
        @Query("userId") userId: String,
        @Query("side") side: Side
    ): Call<GameDTO>

    @GET("init/continue")
    fun continueGame(
        @Query("gameId") gameId: Long,
        @Query("userId") userId: String
    ): Call<GameDTO>
}
