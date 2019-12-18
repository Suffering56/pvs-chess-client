package com.example.chess.ui.custom.chessboard

import com.example.chess.shared.enums.Piece

/**
 * @author v.peschaniy
 *      Date: 18.12.2019
 */

class ConstructorEvent(action: String) {

    var selectedPiece: Piece?
    var removeNext: Boolean

    init {
        selectedPiece = try {
            Piece.valueOf(action)
        } catch (e: Exception) {
            null
        }
        removeNext = action == ACTION_REMOVE
    }

    companion object {
        const val ACTION_REMOVE = "ACTION_REMOVE"
    }
}