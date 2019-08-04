package com.example.chess.network

import com.example.chess.network.api.DebugApi
import com.example.chess.network.api.InitApi

/**
 * @author v.peschaniy
 *      Date: 31.07.2019
 */
interface INetworkService {
    val debugApi: DebugApi
    val initApi: InitApi
}