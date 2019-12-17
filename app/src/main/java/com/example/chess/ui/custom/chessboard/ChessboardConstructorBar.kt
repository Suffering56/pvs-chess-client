package com.example.chess.ui.custom.chessboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.view.children
import com.example.chess.R
import com.example.chess.utils.changeSize
import kotlinx.android.synthetic.main.chessboard_constructor_bar.view.*
import kotlin.streams.asStream
import kotlin.streams.toList

class ChessboardConstructorBar(context: Context?, attrs: AttributeSet?) : TableLayout(context, attrs),
    OnCellSizeChangedObservable.CellSizeChangedEventListener {

    constructor(context: Context) : this(context, null)

    private val cellContainers: List<FrameLayout>
    private val imageItems: List<ImageView>

    var itemClickListener: ((img: ImageView, stateChanged: Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.chessboard_constructor_bar, this, true)

        this.visibility = INVISIBLE

        cellContainers = barTable.children.asStream()
            .map { it as TableRow }
            .flatMap { it.children.asStream() }
            .map { it as FrameLayout }
            .toList()

        imageItems = cellContainers.stream()
            .flatMap { it.children.asStream() }
            .filter { it is ImageView }
            .map { it as ImageView }
            .peek { img ->
                img.setOnClickListener { onItemClick(img) }
            }
            .toList()
    }


    private fun onItemClick(item: ImageView) {
        val alreadySelected = item.background != null
        imageItems.forEach { it.background = null }

        if (!alreadySelected) {
            item.setBackgroundResource(R.drawable.cell_available_attack)
        }

        item.tag

        itemClickListener?.invoke(item, alreadySelected)
    }

    override fun onCellSizeChanged(cellSize: Int) {
        cellContainers.forEach { it.changeSize(cellSize) }
        if (!isShown) {
            this.visibility = View.VISIBLE
        }
    }
}