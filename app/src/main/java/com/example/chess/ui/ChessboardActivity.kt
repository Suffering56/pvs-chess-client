package com.example.chess.ui

import android.os.Bundle
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.chess.R
import com.example.chess.network.INetworkService
import com.example.chess.shared.dto.GameDTO
import com.example.chess.shared.enums.GameMode
import com.example.chess.shared.enums.Side
import com.example.chess.ui.custom.chessboard.ChessboardViewState
import com.example.chess.utils.enqueue
import kotlinx.android.synthetic.main.chessboard_activity.*
import javax.inject.Inject


class ChessboardActivity : BaseActivity() {

    companion object {
        private const val CHESSBOARD_STATE = "CHESSBOARD_STATE"
    }

    @Inject
    lateinit var networkService: INetworkService
    lateinit var game: GameDTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chessboard_activity)
        activityComponent.inject(this)
        ButterKnife.bind(this)

        game = intent.getSerializableExtra(MainActivity.GAME) as GameDTO
        val side = intent.getSerializableExtra(MainActivity.SIDE) as Side?
        val userId = intent.getSerializableExtra(MainActivity.USER_ID) as String

        chessboardView.availablePieceClickHandler = { rowIndex, columnIndex ->
            networkService.gameApi.getAvailableMoves(userId, game.id, rowIndex, columnIndex)
                .enqueue {
                    chessboardView.updateAvailablePoints(it.body()!!)
                }
        }

        chessboardView.applyMoveHandler = { move ->
            networkService.gameApi.applyMove(userId, game.id, move)
                .enqueue {
                    chessboardView.applyStateChanges(it.body()!!)
                    if (game.mode == GameMode.SINGLE) {
                        chessboardView.setSide(chessboardView.getState()?.side?.reverse())
                    }
                }
        }

        Thread {
            networkService.gameApi.getChessboard(userId, game.id)
                .enqueue { response ->
                    response.body()?.let {
                        if (!chessboardView.isInitialized()) {
                            chessboardView.init(it, side)
                        }
                    }
                }
        }.start()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        super.onSaveInstanceState(savedInstanceState)
        chessboardView.getState()?.let { savedInstanceState?.putSerializable(CHESSBOARD_STATE, it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        val chessboardState = savedInstanceState?.get(CHESSBOARD_STATE) as ChessboardViewState?
        chessboardState?.let { chessboardView.init(it) }
    }

    @OnClick(R.id.rotateButton)
    fun rotateChessboard() {
        //TODO: нужен safeCheck, который не будет крашить приложение
        check(game.mode == GameMode.PVP) { "rotation is available only in PVP mode. actual mode: ${game.mode}" }
        chessboardView.getState()!!.side?.let { chessboardView.setSide(it.reverse()) }
    }
}