package com.example.chess.ui.custom.chessboard

import com.example.chess.shared.dto.*
import com.example.chess.shared.enums.Piece
import com.example.chess.shared.enums.Side

/**
 * @author v.peschaniy
 *      Date: 04.09.2019
 */

class ChessboardViewState(chessboard: ChessboardDTO? = null) : IUnmodifiableChessboardViewState {
    lateinit var chessboard: ChessboardDTO

    init {
        chessboard?.let { updateChessboard(it) }
    }

    override var position = 0
    override var side: Side? = null  //if side == null -> isViewer

    var selectedPoint: PointDTO? = null
    var availablePoints: Set<PointDTO>? = null
    var previousMove: MoveDTO? = null
    var checkedPoint: PointDTO? = null

    private fun updateChessboard(chessboard: ChessboardDTO) {
        this.chessboard = chessboard

        position = chessboard.position
        previousMove = chessboard.previousMove
        checkedPoint = chessboard.checkedPoint

        cleanHighlighting()
    }

    internal fun cleanHighlighting() {
        selectedPoint = null
        availablePoints = null
    }

    internal fun applyChanges(changes: ChangesDTO) {
        check(changes.position == position + 1) { "incorrect changes version" }

        position = changes.position
        previousMove = changes.lastMove
        checkedPoint = changes.checkedPoint

        executeMove(changes.lastMove)

        cleanHighlighting()
    }

    private fun executeMove(move: MoveDTO) {
        val cellFrom = chessboard.matrix[move.from.rowIndex][move.from.columnIndex]

        //TODO: вообще плохо пользоваться тем что ChessboardDTO не immutable -> нужно сделать immutable и добавить toBuilder()
        chessboard.matrix[move.from.rowIndex][move.from.columnIndex] = CellDTO(move.from, null)
        chessboard.matrix[move.to.rowIndex][move.to.columnIndex] = cellFrom
    }

    internal fun isAvailablePoint(point: PointDTO) = availablePoints?.contains(point) ?: false

    internal fun isSelfPiece(selectedPiece: Piece?) = side == selectedPiece?.side

    internal fun isSelfTurn() = side == nextTurnSide()

    private fun nextTurnSide() = if (position % 2 == 0) Side.WHITE else Side.BLACK
}