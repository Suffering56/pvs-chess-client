package com.example.chess.ui.custom.chessboard

import android.content.ClipData
import android.content.Context
import android.content.Intent
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
    private val items: List<ImageItem>
    var itemClickListener: ((event: ConstructorEvent) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.chessboard_constructor_bar, this, true)

        cellContainers = barTable.children.asStream()
            .map { it as TableRow }
            .flatMap { it.children.asStream() }
            .map { it as FrameLayout }
            .toList()


        items = cellContainers.stream()
            .map {
                val backView = it.getChildAt(1)
                require(backView.tag == null) { "incorrect backView selected" }
                val img = it.getChildAt(2)
                require(img is ImageView) { "incorrect imageView selected" }

                ImageItem(backView, img)
            }
            .toList()
    }

    inner class ImageItem(
        private val back: View,
        private val img: ImageView
    ) {
        private val action: String = img.tag.toString()

        init {
            @Suppress("ClickableViewAccessibility")
            img.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_MOVE) {
                    tryStartDragAndDrop()
                } else if (event.action == MotionEvent.ACTION_DOWN) {
                    onClick()
                }
                true
            }
        }

        private fun onClick() {
            resetSelection()
            select()
            val event = ConstructorEvent(action)
            itemClickListener?.invoke(event)
        }

        private fun tryStartDragAndDrop() {
            if (ConstructorEvent.isPieceAction(action)) {
                val intent = Intent()
                intent.putExtra(
                    ConstructorEvent.NAME,
                    ConstructorEvent(action)
                )

                val shadowBuilder = DragShadowBuilder(img)

                img.startDragAndDrop(
                    ClipData.newIntent("constructor_bar_event", intent),
                    shadowBuilder,
                    null,
                    0
                )
            }
        }

        private fun select() {
            back.setBackgroundResource(R.drawable.cell_available_attack)
        }

        fun unselect() {
            back.background = null
        }
    }

    private fun resetSelection() {
        items.forEach { it.unselect() }
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