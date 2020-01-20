package com.example.chess.network

import com.example.chess.network.api.GameApi
import com.example.chess.network.api.InitApi

/**
 * @author v.peschaniy
 *      Date: 31.07.2019
 */
interface INetworkService {
    val initApi: InitApi
    val gameApi: GameApi
}