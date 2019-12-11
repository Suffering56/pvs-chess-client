package com.example.chess.ui.custom.chessboard

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import com.example.chess.R
import com.example.chess.shared.enums.PieceType
import com.example.chess.shared.enums.Side

class PawnTransformationChooser(
    context: Context,
    side: Side,
    onChosen: (PieceType) -> Unit
) {
    private val dialog: AlertDialog

    init {
        val customView = LayoutInflater.from(context).inflate(R.layout.pawn_transformation_chooser, null)
        dialog = AlertDialog.Builder(context)
            .setView(customView)
            .setCancelable(false)
            .create()

        for (entry in actionResultToButtonMap) {
            val pieceType = entry.key
            val actionButton = customView.findViewById<ImageView>(entry.value)

            if (side == Side.BLACK) {
                actionButton.setImageResource(blackPiecesByTypeMap.getValue(pieceType))
            }

            actionButton.setOnClickListener {
                onChosen.invoke(pieceType)
                dialog.dismiss()
            }
        }
    }

    fun show() {
        dialog.show()
    }

    companion object {
        val actionResultToButtonMap = mapOf(
            Pair(PieceType.KNIGHT, R.id.chooserKnightButton),
            Pair(PieceType.BISHOP, R.id.chooserBishopButton),
            Pair(PieceType.ROOK, R.id.chooserRookButton),
            Pair(PieceType.QUEEN, R.id.chooserQueenButton)
        )

        val blackPiecesByTypeMap = mapOf(
            Pair(PieceType.KNIGHT, R.drawable.knight_black),
            Pair(PieceType.BISHOP, R.drawable.bishop_black),
            Pair(PieceType.ROOK, R.drawable.rook_black),
            Pair(PieceType.QUEEN, R.drawable.queen_black)
        )
    }
}