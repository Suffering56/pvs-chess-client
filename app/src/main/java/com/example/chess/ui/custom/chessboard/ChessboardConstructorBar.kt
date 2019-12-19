package com.example.chess.ui.custom.chessboard

import android.content.ClipData
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
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

class ChessboardConstructorBar(
    context: Context?, attrs: AttributeSet?
) : TableLayout(context, attrs),
    OnCellSizeChangedObservable.CellSizeChangedEventListener, OnCellSizeChangedObservable {

    constructor(context: Context) : this(context, null)

    private var visibilityState: VisibilityState = VisibilityState(attrs)
    override var listeners: MutableList<OnCellSizeChangedObservable.CellSizeChangedEventListener> = mutableListOf()

    private val cellContainers: List<FrameLayout>
    private val imageItems: List<ImageView>
    var itemClickListener: ((event: ConstructorEvent) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.chessboard_constructor_bar, this, true)

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
                img.setOnTouchListener { view, event -> onStartDragAndDrop(view as ImageView, event); true }
            }
            .toList()
    }

    private fun onStartDragAndDrop(img: ImageView, event: MotionEvent) {
        if (ConstructorEvent.isPieceAction(img.tag.toString())) {
            img.startDragAndDrop(
                ClipData.newPlainText("label", "text"),
                DragShadowBuilder(img),
                null,
                0
            )
        }
    }

    private fun onItemClick(item: ImageView) {
        resetSelection()
        val action = selectItem(item)
        itemClickListener?.invoke(ConstructorEvent(action)) //TODO: нужно добавить callback, чтобы ресетать селекшн,
    }

    private fun isItemSelected(item: ImageView) = item.background != null

    private fun selectItem(item: ImageView): String {
        item.setBackgroundResource(R.drawable.cell_available_attack)
        return item.tag.toString()
    }

    private fun resetSelection() {
        imageItems.forEach { it.background = null }
    }

    override fun onCellSizeChanged(cellSize: Int) {
        cellContainers.forEach { it.changeSize(cellSize) }
        if (!visibilityState.sizeDefined) {
            visibilityState.sizeDefined = true
        }
        notify(cellSize)
    }

    override fun setVisibility(visibility: Int) {
        visibilityState.visibility = visibility

        if (visibilityState.sizeDefined || visibility != View.VISIBLE) {
            super.setVisibility(visibility)
        }
    }

    /**
     * Костыль, призванный не изменять размер chessboardBar-а на глазах у пользователя.
     * На основе этого состояния реализовано отложенное отображение текущего компонента
     */
    private class VisibilityState(attrs: AttributeSet?) {
        var visibility: Int = extractVisibilityFromAttrs(attrs)
        var sizeDefined: Boolean = false

        private fun extractVisibilityFromAttrs(attrs: AttributeSet?): Int {
            val defaultVisibility = VISIBLE
            val defaultValue = defaultVisibility / VISIBILITY_CONVERTER_MAGIC_NUMBER

            val attrsVisibleValue: Int = attrs?.getAttributeIntValue(
                NAMESPACE_ANDROID,
                "visibility",
                defaultValue
            ) ?: defaultValue

            return attrsVisibleValue * VISIBILITY_CONVERTER_MAGIC_NUMBER
        }

        companion object {
            private const val NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
            private const val VISIBILITY_CONVERTER_MAGIC_NUMBER = 4
        }
    }
}