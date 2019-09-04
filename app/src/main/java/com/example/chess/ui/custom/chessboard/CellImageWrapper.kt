package com.example.chess.ui.custom.chessboard

import android.widget.ImageView
import com.example.chess.R
import com.example.chess.shared.dto.PointDTO
import com.example.chess.shared.enums.Piece

/**
 * @author v.peschaniy
 *      Date: 04.09.2019
 */

internal class CellImageWrapper(
    val rowIndex: Int,
    val columnIndex: Int,
    val img: ImageView
) {
    companion object {
        private const val TRANSPARENT = R.color.colorAbsoluteTransparency
    }

    val point get() = PointDTO(rowIndex, columnIndex)
    var piece: Piece? = null
        set(value) {
            field = value
            img.setImageResource(convertPieceToResource(piece))
        }

    init {
        img.setImageResource(TRANSPARENT)
    }

    fun markSelected() {
        requireNotNull(piece)
        img.setBackgroundResource(R.color.chessboardCellSelected)
    }

    fun markAvailable() {
        if (piece == null) {
            img.setBackgroundResource(R.drawable.cell_available_empty)
        } else {
            img.setBackgroundResource(R.drawable.cell_available_attack_2)
        }
    }

    fun markPrevious() {
        img.setBackgroundResource(R.color.chessboardCellPrevious)
    }

    fun markChecked() {
        img.setBackgroundResource(R.drawable.cell_checked_2)
    }

    fun unmark() {
        img.setBackgroundResource(TRANSPARENT)
    }

    private fun convertPieceToResource(piece: Piece?): Int {
        return when (piece) {
            Piece.PAWN_WHITE -> R.drawable.pawn_white
            Piece.PAWN_BLACK -> R.drawable.pawn_black
            Piece.KNIGHT_WHITE -> R.drawable.knight_white
            Piece.KNIGHT_BLACK -> R.drawable.knight_black
            Piece.BISHOP_WHITE -> R.drawable.bishop_white
            Piece.BISHOP_BLACK -> R.drawable.bishop_black
            Piece.ROOK_WHITE -> R.drawable.rook_white
            Piece.ROOK_BLACK -> R.drawable.rook_black
            Piece.QUEEN_WHITE -> R.drawable.queen_white
            Piece.QUEEN_BLACK -> R.drawable.queen_black
            Piece.KING_WHITE -> R.drawable.king_white
            Piece.KING_BLACK -> R.drawable.king_black
            null -> TRANSPARENT
        }
    }
}