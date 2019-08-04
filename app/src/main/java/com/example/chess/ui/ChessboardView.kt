package com.example.chess.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TableRow
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.chess.R
import com.example.chess.shared.dto.*
import com.example.chess.shared.enums.Piece
import com.example.chess.shared.enums.Side
import com.example.chess.utils.changeSize
import kotlinx.android.synthetic.main.chessboard_view.view.*
import java.io.Serializable
import java.util.*


class ChessboardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    companion object {
        private const val BOARD_SIZE = 8
        private const val TRANSPARENT = R.color.colorAbsoluteTransparency
    }

    private val cellsMatrices: Array<Array<CellImageWrapper>>
    private val cellsStream get() = Arrays.stream(cellsMatrices).flatMap { Arrays.stream(it) }

    private lateinit var state: State
    fun isInitialized() = ::state.isInitialized
    fun getState() = if (isInitialized()) state else null   //TODO: IUnmodifiableState

    var getAvailableMovesListener: ((rowIndex: Int, columnIndex: Int) -> Set<PointDTO>)? = null
    var applyMoveListener: ((move: MoveDTO) -> ChangesDTO)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.chessboard_view, this, true)

        cellsMatrices = Array(BOARD_SIZE) row@{ rowIndex ->
            val tableRow = TableRow(context)

            val imagesArray = Array(BOARD_SIZE) cell@{ columnIndex ->
                val img = ImageView(context)
                val cellContainer = CellImageWrapper(rowIndex, columnIndex, img)

                img.setImageResource(TRANSPARENT)
                img.setOnClickListener { onCellClick(rowIndex, columnIndex) }

                tableRow.addView(img)

                return@cell cellContainer
            }
            chessboardTable.addView(tableRow)

            return@row imagesArray
        }
    }

    fun init(chessboard: ChessboardDTO) {
        check(!isInitialized())

        this.state = State(chessboard)
        setSide(Side.WHITE)

        updateViewByState()
    }

    fun init(state: State) {
        check(!isInitialized())

        this.state = state
        setSide(Side.WHITE)

        updateViewByState()
    }

    private fun updateViewByState() {
        cellsStream.forEach {
            it.piece = state.chessboard.matrix[it.rowIndex][it.columnIndex].piece
        }

        cellsStream.forEach { it.unmark() }

        state.previousMove?.let {
            getCell(it.from).markPrevious()
            getCell(it.to).markPrevious()
        }

        state.checkedPoint?.let {
            getCell(it).markChecked()
        }

        state.selectedPoint?.let { getCell(it).markSelected() }

        state.availablePoints?.let { availablePoints ->
            cellsStream
                .filter { availablePoints.contains(it.point) }
                .forEach { it.markAvailable() }
        }

        chessboardProgressBar.visibility = View.INVISIBLE
    }

    fun setSide(side: Side) {
        state.side = side

        rotation = when (side) {
            Side.WHITE -> 180f
            Side.BLACK -> 0f
        }
    }

    override fun setRotation(rotation: Float) {
        super.setRotation(rotation)
        cellsStream.forEach { it.img.rotation = rotation }
    }

    private fun getCell(point: PointDTO): CellImageWrapper {
        return cellsMatrices[point.rowIndex][point.columnIndex]
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val cellSize = w / BOARD_SIZE

        this.post {
            chessboardProgressBar.changeSize(cellSize * 2)

            Arrays.stream(cellsMatrices)
                .flatMap { Arrays.stream(it) }
                .forEach { it.img.changeSize(cellSize) }
        }
    }

    private fun onCellClick(rowIndex: Int, columnIndex: Int) {
        println("onCellClick: $rowIndex,$columnIndex")

        val selectedCell = cellsMatrices[rowIndex][columnIndex]
        val selectedPoint = selectedCell.point

        if (state.isAvailablePoint(selectedPoint)) {
            val move = MoveDTO(state.selectedPoint!!, selectedPoint)

            val changes = applyMoveListener?.invoke(move)
                ?: ChangesDTO(1, move, PointDTO(7, 3))

            state.applyChanges(changes)

        } else if (selectedPoint == state.selectedPoint || !state.isSelfPiece(selectedCell.piece) || !state.isSelfTurn()) {
            state.cleanHighlighting()

        } else {
            state.availablePoints = getAvailableMovesListener?.invoke(rowIndex, columnIndex)
            state.selectedPoint = selectedPoint
        }

        updateViewByState()
    }

    private class CellImageWrapper(
        val rowIndex: Int,
        val columnIndex: Int,
        val img: ImageView
    ) {
        val point get() = PointDTO(rowIndex, columnIndex)
        var piece: Piece? = null
            set(value) {
                field = value
                img.setImageResource(convertPieceToResource(piece))
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

        //TODO: markMouseOver() -> only for available

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

    class State(chessboard: ChessboardDTO? = null) : Serializable {
        lateinit var chessboard: ChessboardDTO

        init {
            chessboard?.let { updateChessboard(it) }
        }

        var position = 0
        var side: Side = Side.WHITE

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

        internal fun isSelfPiece(selectedPiece: Piece?) = selectedPiece?.side == side

        internal fun isSelfTurn() = nextTurnSide() == side

        private fun nextTurnSide() = if (position % 2 == 0) Side.WHITE else Side.BLACK
    }
}