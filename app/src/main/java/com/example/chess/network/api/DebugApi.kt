package com.example.chess.network.api

import com.example.chess.shared.dto.ChessboardDTO
import com.example.chess.shared.dto.PointDTO
import retrofit2.Call
import retrofit2.http.GET

/**
 * @author v.peschaniy
 * Date: 16.07.2019
 */
interface DebugApi {

    @GET("debug/version")
    fun getVersion(): Call<String>

    @GET("debug/point")
    fun getPoint(): Call<PointDTO>

    @GET("debug/chessboard")
    fun getChessboard(): Call<ChessboardDTO>
}
