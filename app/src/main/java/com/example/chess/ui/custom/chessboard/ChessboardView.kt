package com.example.chess.ui.custom.chessboard

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TableRow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.example.chess.R
import com.example.chess.shared.dto.ChangesDTO
import com.example.chess.shared.dto.ChessboardDTO
import com.example.chess.shared.dto.MoveDTO
import com.example.chess.shared.dto.PointDTO
import com.example.chess.shared.enums.PieceType
import com.example.chess.shared.enums.Side
import com.example.chess.utils.changeSize
import kotlinx.android.synthetic.main.chessboard_view.view.*
import java.util.*


class ChessboardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    companion object {
        private const val BOARD_SIZE = 8
    }

    private val cellsMatrices: Array<Array<CellImageWrapper>>
    private val cellsStream get() = Arrays.stream(cellsMatrices).flatMap { Arrays.stream(it) }

    private lateinit var state: ChessboardViewState
    fun isInitialized() = ::state.isInitialized
    fun getState(): IUnmodifiableChessboardViewState? = if (isInitialized()) state else null

    var availablePieceClickHandler: ((rowIndex: Int, columnIndex: Int) -> Unit)? = null
    var applyMoveHandler: ((move: MoveDTO) -> Unit)? = null

    private val legendOffset = resources.getDimension(R.dimen.chessboard_offset_for_legend).toInt()

    init {
        LayoutInflater.from(context).inflate(R.layout.chessboard_view, this, true)

        cellsMatrices = Array(BOARD_SIZE) row@{ rowIndex ->
            val tableRow = TableRow(context)

            val imagesArray = Array(BOARD_SIZE) cell@{ columnIndex ->
                val img = ImageView(context)
                val cellContainer = CellImageWrapper(rowIndex, columnIndex, img)

                img.setOnClickListener {
                    println("onCellClick: $rowIndex,$columnIndex")
                    onCellClick(rowIndex, columnIndex)
                    repaint()
                }

                tableRow.addView(img)

                return@cell cellContainer
            }
            chessboardTable.addView(tableRow)

            return@row imagesArray
        }
    }

    fun init(chessboard: ChessboardDTO, side: Side?) {
        check(!isInitialized())

        this.state = ChessboardViewState(chessboard)
        setSide(side, true)

        repaint()
    }

    fun init(state: ChessboardViewState) {
        check(!isInitialized())

        this.state = state
        setSide(state.side, true)

        repaint()
    }

    fun updateAvailablePoints(availablePoints: Set<PointDTO>) {
        state.availablePoints = availablePoints
        repaint()
    }

    fun applyStateChanges(changes: ChangesDTO) {
        state.applyChanges(changes)
        repaint()
    }

    fun setSide(side: Side?, isNeedToRotate: Boolean) {
        state.side = side

        if (isNeedToRotate) {
            rotation = when (side) {
                Side.WHITE -> 180f
                Side.BLACK -> 0f
                else -> 180f
            }
        }
    }

    private fun onCellClick(rowIndex: Int, columnIndex: Int) {
        val selectedCell = cellsMatrices[rowIndex][columnIndex]
        val selectedPoint = selectedCell.point

        if (state.isAvailablePoint(selectedPoint)) {
            //кликнули по доступной для хода ячейки - значит нужно выполнить ход

            if ((rowIndex == 0 || rowIndex == 7) && getCell(state.selectedPoint!!).piece!!.isPawn()) {
                showPawnTransformationChooser {
                    applyMoveHandler?.invoke(MoveDTO(state.selectedPoint!!, selectedPoint, it))
                }
            } else {
                applyMoveHandler?.invoke(MoveDTO(state.selectedPoint!!, selectedPoint, null))
            }
        } else if (selectedPoint == state.selectedPoint || !state.isSelfPiece(selectedCell.piece) || !state.isSelfTurn()) {
            state.cleanHighlighting()

        } else {
            //кликнули на свою фигуру, и это был не повторный клик - значит хотим запросить доступные ходы
            availablePieceClickHandler?.invoke(rowIndex, columnIndex)
            state.selectedPoint = selectedPoint
        }
    }


    private fun showPawnTransformationChooser(onChosen: (piece: PieceType) -> Unit) {
        val items = arrayOf(PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK, PieceType.QUEEN)

        AlertDialog.Builder(context)
            .setTitle("title")
            .setItems(items.map { it.name }.toTypedArray()) { _, which ->
                onChosen.invoke(items[which])
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun repaint() {
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

    private fun getCell(point: PointDTO): CellImageWrapper {
        return cellsMatrices[point.row][point.col]
    }

    /**
     * Переворачиваем только chessboardTable
     */
    override fun setRotation(rotation: Float) {
        chessboardTable.rotation = rotation
        cellsStream.forEach { it.img.rotation = rotation }

        val legendRotation = 180f - rotation
        for (legend in sequenceOf(legendLeft, legendRight, legendTop, legendBottom)) {
            legend.rotation = legendRotation
            for (child in legend.children) {
                child.rotation = legendRotation
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val cellSize = (w - legendOffset * 2) / BOARD_SIZE

        this.post {
            chessboardProgressBar.changeSize(cellSize * 2)

            Arrays.stream(cellsMatrices)
                .flatMap { Arrays.stream(it) }
                .forEach { it.img.changeSize(cellSize) }

            for (child in legendTop.children + legendBottom.children) {
                child.changeSize(cellSize, legendOffset)
            }

            for (child in legendLeft.children + legendRight.children) {
                child.changeSize(legendOffset, cellSize)
            }
        }
    }
}