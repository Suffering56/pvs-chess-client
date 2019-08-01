package com.example.chess.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TableRow
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.chess.R
import com.example.chess.shared.dto.ChessboardDTO
import com.example.chess.shared.dto.PointDTO
import com.example.chess.shared.enums.Piece
import kotlinx.android.synthetic.main.chessboard_view.view.*
import java.util.*


class ChessboardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val BOARD_SIZE = 8
        private const val PIECE_NONE = R.color.colorAbsoluteTransparency
    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    lateinit var getAvailableMovesListener: (rowIndex: Int, columnIndex: Int) -> Set<PointDTO>

    private val cellImagesMatrix: Array<Array<CellContainer>>

    init {
        LayoutInflater.from(context).inflate(R.layout.chessboard_view, this, true)

        cellImagesMatrix = Array(BOARD_SIZE) row@{ rowIndex ->
            val tableRow = TableRow(context)

            val imagesArray = Array(BOARD_SIZE) cell@{ columnIndex ->
                val img = ImageView(context)
                img.setImageResource(PIECE_NONE)
                img.setOnClickListener { onCellClick(rowIndex, columnIndex) }
                tableRow.addView(img)

                return@cell CellContainer(rowIndex, columnIndex, img)
            }
            chessboardTable.addView(tableRow)

            return@row imagesArray
        }

        rotation = 180f //представление по умолчанию: игрок играет белыми (поэтому они должны быть снизу)
    }

    fun update(dto: ChessboardDTO) {
        forEachCell {
            val piece = dto.matrix[it.rowIndex][it.columnIndex].piece
            it.img.setImageResource(convertPieceToResource(piece))
        }
    }

    private fun forEachCell(eachConsumer: (cell: CellContainer) -> Unit) {
        cellImagesMatrix.forEach {
            it.forEach(eachConsumer)
        }
    }

    override fun setRotation(rotation: Float) {
        super.setRotation(rotation)
        forEachCell { it.img.rotation = rotation }
    }

    private fun getCell(point: PointDTO): CellContainer {
        return cellImagesMatrix[point.rowIndex][point.columnIndex]
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val cellSize = w / BOARD_SIZE       //TODO: возможно стоит учитывать portrait/landscape ориентацию смартфона

        this.post {
            Arrays.stream(cellImagesMatrix)
                .flatMap { Arrays.stream(it) }
                .forEach {
                    it.img.layoutParams.width = cellSize
                    it.img.layoutParams.height = cellSize
                    it.img.requestLayout()
                }
        }
    }

    private fun onCellClick(rowIndex: Int, columnIndex: Int) {
        println("rowIndex = $rowIndex")
        println("rowIndex = $columnIndex")
        cellImagesMatrix[rowIndex][columnIndex].img.setImageResource(R.color.colorLightTransparency)

        if (::getAvailableMovesListener.isInitialized) {
            val availablePoints = getAvailableMovesListener.invoke(rowIndex, columnIndex)
        }
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
            null -> PIECE_NONE
        }
    }

    private class CellContainer(
        val rowIndex: Int,
        val columnIndex: Int,
        val img: ImageView
    ) {
        var selected = false        //повторный клик убирает выделение и снимает доступные ходы
        var available = false       //available-empty + available-harmful
        //mouseOver -> only for available
        //opponentPrevMove.from-> to
        //checked
    }
}