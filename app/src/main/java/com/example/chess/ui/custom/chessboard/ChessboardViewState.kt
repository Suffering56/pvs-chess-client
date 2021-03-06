package com.example.chess.ui.custom.chessboard

import com.example.chess.shared.dto.*
import com.example.chess.shared.enums.Piece
import com.example.chess.shared.enums.Side

/**
 * @author v.peschaniy
 *      Date: 04.09.2019
 */

class ChessboardViewState(
    chessboard: ChessboardDTO? = null,
    initialPosition: Int
) : IUnmodifiableChessboardViewState {

    override lateinit var chessboard: ChessboardDTO

    init {
        chessboard?.let { updateChessboard(it) }
    }

    override var position = initialPosition
    override var side: Side? = null  //if side == null -> isViewer

    var selectedPoint: PointDTO? = null
    var availablePoints: Set<PointDTO>? = null
    var previousMove: MoveDTO? = null
    var checkedPoint: PointDTO? = null

    var constructorState: ConstrictorState? = null

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

    internal fun executeConstructorMove(pointTo: PointDTO) {
        val state = requireNotNull(constructorState) {
            "try to execute constructor move, but constructor state is null"
        }

        val pointFrom = state.movePointFrom

        pointFrom?.let {
            // перемещение фигуры в рамках доски. очищаем pointFrom
            chessboard.matrix[it.row][it.col] = CellDTO(pointTo, null)
        }

        // выставляем запомненную фигуру на доску (это или перемещаемая или новая)
        // или удаляем ту что была на указанной точке в случае (removeNext == true)
        chessboard.matrix[pointTo.row][pointTo.col] = CellDTO(
            pointTo,
            if (state.removeNext) null else state.piece
        )

        state.movePointFrom?.let {
            state.piece = null
        }
        state.movePointFrom = null
        cleanHighlighting()
    }

    internal fun applyChanges(changes: ChangesDTO) {
        check(changes.position == position + 1) { "incorrect changes version, expected: ${position + 1}, actual: ${changes.position}" }

        position = changes.position
        previousMove = changes.lastMove
        checkedPoint = changes.checkedPoint

        executeMove(changes.lastMove)
        changes.additionalMove?.let { executeMove(it) }

        cleanHighlighting()
    }

    private fun executeMove(move: MoveDTO) {
        val pieceFrom = chessboard.matrix[move.from.row][move.from.col].piece

        //TODO: вообще плохо пользоваться тем что ChessboardDTO не immutable -> нужно сделать immutable и добавить toBuilder()
        chessboard.matrix[move.from.row][move.from.col] = CellDTO(move.from, null)

        if (isCutMove(move)) {
            return
        }

        if (move.pawnTransformationPiece == null) {
            chessboard.matrix[move.to.row][move.to.col] = CellDTO(move.to, pieceFrom)
        } else {
            chessboard.matrix[move.to.row][move.to.col] = CellDTO(
                move.to,
                move.pawnTransformationPiece
            )
        }
    }

    internal fun isAvailablePoint(point: PointDTO) = availablePoints?.contains(point) ?: false

    internal fun isSelfPiece(selectedPiece: Piece?) = side == selectedPiece?.side

    internal fun isSelfTurn() = side == Side.nextTurnSide(position)

    private fun isCutMove(move: MoveDTO) = move.from == move.to

    internal fun enableConstructor() {
        constructorState = ConstrictorState()
    }

    internal fun disableConstructor() {
        constructorState = null
    }

    inner class ConstrictorState {
        var piece: Piece? = null
        var movePointFrom: PointDTO? = null
        var removeNext: Boolean = false

        internal fun update(event: ConstructorEvent) {
            removeNext = event.removeNext
            piece = event.selectedPiece
        }
    }
}