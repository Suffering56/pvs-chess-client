package com.example.chess.network.api

import com.example.chess.shared.dto.GameDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * @author v.peschaniy
 * Date: 16.07.2019
 */
interface InitApi {

    @GET("init")
    fun createGame(): Call<GameDTO>

    @GET("init/{gameId}")
    fun getGame(@Path("gameId") gameId: Long): Call<GameDTO>

//    @GET("init/chessboard")
//    fun getChessboard(): Call<ChessboardDTO>
}
