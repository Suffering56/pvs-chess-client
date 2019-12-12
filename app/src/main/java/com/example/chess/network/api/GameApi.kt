package com.example.chess.network.api

import com.example.chess.shared.dto.ChangesDTO
import com.example.chess.shared.dto.ChessboardDTO
import com.example.chess.shared.dto.MoveDTO
import com.example.chess.shared.dto.PointDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author v.peschaniy
 *      Date: 02.09.2019
 */

interface GameApi {

    @GET("game/move")
    fun getAvailableMoves(
        @Query("userId") userId: String,
        @Query("gameId") gameId: Long,
        @Query("rowIndex") rowIndex: Int,
        @Query("columnIndex") columnIndex: Int
    ): Call<Set<PointDTO>>

    @POST("game/move")
    fun applyMove(
        @Query("userId") userId: String,
        @Query("gameId") gameId: Long,
        @Body move: MoveDTO
    ): Call<ChangesDTO>

    @GET("game/chessboard")
    fun getChessboard(
        @Query("userId") userId: String,
        @Query("gameId") gameId: Long,
        @Query("position") position: Int? = null
    ): Call<ChessboardDTO>

    @GET("game/chessboard")
    fun rollback(
        @Query("userId") userId: String,
        @Query("gameId") gameId: Long,
        @Query("positionsOffset") positionsOffset: Int
    ): Call<ChessboardDTO>
}