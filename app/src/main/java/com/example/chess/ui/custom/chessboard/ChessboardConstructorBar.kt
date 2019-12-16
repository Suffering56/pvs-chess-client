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

    private val cellComponentsList: List<View>
    private val items get() = cellComponentsList.stream().filter { it is ImageView }.map { it as ImageView }

    var itemClickListener: ((img: ImageView, stateChanged: Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.chessboard_constructor_bar, this, true)

        cellComponentsList = barTable.children.asStream()
            .map { it as TableRow }
            .flatMap { it.children.asStream() }
            .map { it as FrameLayout }
            .flatMap { it.children.asStream() }
            .toList()

        items.forEach { img ->
            img.setOnClickListener { onItemClick(img) }
        }
    }


    private fun onItemClick(item: ImageView) {
        val alreadySelected = item.background != null
        items.forEach { it.background = null }

        if (!alreadySelected) {
            item.setBackgroundResource(R.drawable.cell_available_attack)
        }

        itemClickListener?.invoke(item, alreadySelected)
    }

    override fun onCellSizeChanged(cellSize: Int) {
        cellComponentsList.forEach { it.changeSize(cellSize) }
    }
}