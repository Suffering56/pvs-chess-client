package com.example.chess.ui

import android.os.Bundle
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.chess.R
import com.example.chess.network.INetworkService
import com.example.chess.shared.dto.ChessboardDTO
import com.example.chess.shared.dto.GameDTO
import com.example.chess.shared.enums.GameMode
import com.example.chess.shared.enums.Side
import com.example.chess.ui.custom.chessboard.ChessboardViewState
import com.example.chess.ui.custom.chessboard.OnCellSizeChangedObservable.CellSizeChangedEventListener
import com.example.chess.utils.enqueue
import kotlinx.android.synthetic.main.chessboard_activity.*
import javax.inject.Inject
import kotlin.properties.Delegates

class ChessboardActivity : BaseActivity(), CellSizeChangedEventListener {

    companion object {
        private const val CHESSBOARD_STATE = "CHESSBOARD_STATE"
        private const val SINGLE_MOVE_AUTO_ROTATION_ENABLED = false
    }

    @Inject
    lateinit var networkService: INetworkService

    private lateinit var userId: String
    private var gameId: Long by Delegates.notNull()
    private lateinit var gameMode: GameMode

    @Suppress("PLUGIN_WARNING") //TODO: ругается что chessboardConstructorBar может быть null, но при этом не ругается на остальные компоненты
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chessboard_activity)
        activityComponent.inject(this)
        ButterKnife.bind(this)

        val game = intent.getSerializableExtra(MainActivity.GAME) as GameDTO
        val side = intent.getSerializableExtra(MainActivity.SIDE) as Side?
        userId = intent.getSerializableExtra(MainActivity.USER_ID) as String
        gameId = game.id
//        gameMode = game.mode
        gameMode = GameMode.CONSTRUCTOR

        chessboardConstructorBar.visibility = View.INVISIBLE
        chessboardView.subscribe(chessboardConstructorBar)
        chessboardConstructorBar.subscribe(this)

        chessboardConstructorBar.itemClickListener = { action ->
            chessboardView.updateConstructorState(action)
        }

        chessboardView.availablePieceClickHandler = { rowIndex, columnIndex ->
            networkService.gameApi.getAvailableMoves(userId, gameId, rowIndex, columnIndex)
                .enqueue {
                    chessboardView.updateAvailablePoints(it.body()!!)
                }
        }

        chessboardView.applyMoveHandler = { move ->
            networkService.gameApi.applyMove(userId, gameId, move)
                .enqueue {
                    val changes = it.body()!!
                    chessboardView.applyStateChanges(changes)
                    //TODO: game.position = changes.position

                    if (gameMode == GameMode.SINGLE) {
                        chessboardView.setSide(
                            chessboardView.getState()?.side?.reverse(),
                            SINGLE_MOVE_AUTO_ROTATION_ENABLED
                        )
                    }
                }
        }

        initChessboardContent(gameId, side)
    }

    private fun initChessboardContent(gameId: Long, side: Side?) {
        if (gameMode != GameMode.CONSTRUCTOR) {
            Thread {
                networkService.gameApi.getChessboard(userId, gameId)
                    .enqueue { response ->
                        response.body()?.let {
                            if (!chessboardView.isInitialized()) {
                                chessboardView.init(it, side)
                            }
                        }
                    }
            }.start()
        } else {
            chessboardView.init(
                ChessboardDTO(
                    0,
                    ChessboardDTO.createEmptyMatrix(),
                    null,
                    null
                ),
                side
            )

            chessboardView.enableConstructorMode()
        }
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
        check(gameMode == GameMode.PVP) { "rotation is available only in PVP mode. actual mode: $gameMode" }
        chessboardView.getState()!!.side?.let { chessboardView.setSide(it.reverse(), true) }
    }

    @OnClick(R.id.rollbackButton)
    fun rollback() {
        Thread {
            networkService.gameApi.rollback(userId, gameId, 1)
                .enqueue { response ->
                    response.body()?.let {
                        chessboardView.resetTo(it)
                    }
                }
        }.start()
    }

    /**
     * Отложенное появление панели конструтора доски
     */
    override fun onCellSizeChanged(cellSize: Int) {
        if (gameMode == GameMode.CONSTRUCTOR) {
            @Suppress("PLUGIN_WARNING")
            chessboardConstructorBar.visibility = View.VISIBLE
        }
    }
}