package com.example.chess.ui

import android.os.Bundle
import com.example.chess.R
import com.example.chess.network.INetworkService
import javax.inject.Inject

class ChessboardActivity : BaseActivity() {
    @Inject
    lateinit var networkService: INetworkService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chessboard)
        activityComponent.inject(this)
    }
}
