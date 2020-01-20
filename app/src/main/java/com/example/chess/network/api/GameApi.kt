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

    @GET("game/moves")
    fun getAvailableMoves(
        @Query("gameId") gameId: Long,
        @Query("userId") userId: String,
        @Query("clientPosition") clientPosition: Int,
        @Query("rowIndex") rowIndex: Int,
        @Query("columnIndex") columnIndex: Int
    ): Call<Set<PointDTO>>

    @POST("game/move")
    fun applyMove(
        @Query("gameId") gameId: Long,
        @Query("userId") userId: String,
        @Query("clientPosition") clientPosition: Int,
        @Body move: MoveDTO
    ): Call<ChangesDTO>

    @POST("game/rollback")
    fun rollback(
        @Query("gameId") gameId: Long,
        @Query("userId") userId: String,
        @Query("clientPosition") clientPosition: Int,
        @Query("positionsOffset") positionsOffset: Int
    ): Call<ChessboardDTO>

    @GET("game/listen")
    fun listenOpponentChanges(
        @Query("gameId") gameId: Long,
        @Query("userId") userId: String,
        @Query("clientPosition") clientPosition: Int
    ): Call<ChangesDTO>

    @GET("game/chessboard")
    fun getChessboard(
        @Query("gameId") gameId: Long,
        @Query("userId") userId: String,
        @Query("position") position: Int? = null
    ): Call<ChessboardDTO>
}