package com.example.chess.ui.custom.chessboard

import com.example.chess.shared.enums.Side
import java.io.Serializable

/**
 * @author v.peschaniy
 *      Date: 04.09.2019
 */
interface IUnmodifiableChessboardViewState : Serializable {
    val position: Int
    val side: Side?
}