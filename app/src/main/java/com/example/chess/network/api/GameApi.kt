package com.example.chess.network.api

import com.example.chess.shared.dto.PointDTO
import retrofit2.Call
import retrofit2.http.GET
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
}