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
        selectedPiece = if (isPieceAction(action)) Piece.valueOf(action) else null

        removeNext = action == ACTION_REMOVE
    }

    companion object {
        const val ACTION_REMOVE = "ACTION_REMOVE"
        private const val ACTION_MOVE = "ACTION_MOVE"

        fun isPieceAction(action: String) = action != ACTION_REMOVE && action != ACTION_MOVE
    }
}