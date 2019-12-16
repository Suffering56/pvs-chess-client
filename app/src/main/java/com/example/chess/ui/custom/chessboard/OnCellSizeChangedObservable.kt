package com.example.chess.ui.custom.chessboard

interface OnCellSizeChangedObservable {

    interface CellSizeChangedEventListener {
        fun onCellSizeChanged(cellSize: Int)
    }

    var listeners: MutableList<CellSizeChangedEventListener>

    fun subscribe(listener: CellSizeChangedEventListener) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: CellSizeChangedEventListener) {
        listeners.remove(listener)
    }

    fun notify(cellSize: Int) {
        for (listener in listeners) {
            listener.onCellSizeChanged(cellSize)
        }
    }
}
