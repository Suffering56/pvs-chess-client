package com.example.chess.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.chess.App
import com.example.chess.GameState
import com.example.chess.R
import com.example.chess.network.INetworkService
import com.example.chess.shared.dto.ChangesDTO
import com.example.chess.shared.dto.ChessboardDTO
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
        private const val LISTEN_OPPONENT_CHANGES_TICK_INTERVAL = 3000L
    }

    @Inject
    lateinit var networkService: INetworkService

    private var game: GameState by Delegates.notNull()

    private val userId: String get() = game.userId
    private val gameId: Long get() = game.id!!
    private val gameMode: GameMode get() = game.mode
    private val side: Side get() = game.side!!
    private var currentPosition: Int = 0

    @SuppressLint("SetTextI18n")
    @Suppress("PLUGIN_WARNING") //TODO: ругается что chessboardConstructorBar может быть null, но при этом не ругается на остальные компоненты
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chessboard_activity)
        activityComponent.inject(this)
        ButterKnife.bind(this)

        game = intent.getSerializableExtra(App.EXTRAS_GAME) as GameState

        if (!game.isConstructor) {
            gameIdView.text = "gameId: $gameId"
        } else {
            gameIdView.text = "gameId: ???"
        }

        chessboardConstructorBar.visibility = View.INVISIBLE
        chessboardView.subscribe(chessboardConstructorBar)
        chessboardConstructorBar.subscribe(this)

        chessboardConstructorBar.itemClickListener = { event ->
            chessboardView.updateConstructorState(event)
        }

        chessboardConstructorBar.onDisableConstructorListener = {
            val chessboard = chessboardView.getState()!!.chessboard

            networkService.initApi.createConstructorGame(userId, gameMode, side, chessboard)
                .enqueue {
                    val constructorGame = it.body()!!
                    //TODO: constructorGame.matrix == chessboard.matrix

                    game.id = constructorGame.gameId
                    currentPosition = constructorGame.position

                    val newChessboard = ChessboardDTO(
                        constructorGame.position,
                        constructorGame.matrix,
                        null,
                        constructorGame.checkedPoint
                    )

                    chessboardView.resetTo(newChessboard)
                    chessboardView.disableConstructorMode()

                    chessboardConstructorBar.visibility = View.INVISIBLE
                    gameIdView.text = "gameId: $gameId"
                }
        }

        chessboardView.availablePieceClickHandler = { rowIndex, columnIndex ->
            networkService.gameApi.getAvailableMoves(gameId, userId, currentPosition, rowIndex, columnIndex)
                .enqueue {
                    chessboardView.updateAvailablePoints(it.body()!!)
                }
        }

        chessboardView.applyMoveHandler = { move ->
            networkService.gameApi.applyMove(gameId, userId, currentPosition, move)
                .enqueue {
                    val changes = it.body()!!
                    applyMove(changes)
                }
        }

        initChessboardContent()

        if ((gameMode == GameMode.PVP || gameMode == GameMode.AI)) {
            chessboardView.initOpponentChangesListener(LISTEN_OPPONENT_CHANGES_TICK_INTERVAL) {
                if (game.id != null && side != Side.nextTurnSide(currentPosition)) {
                    networkService.gameApi.listenOpponentChanges(gameId, userId, currentPosition)
                        .enqueue { response ->
                            val changes = response.body()!!
//                            println("listen changes response: $changes")
                            if (!changes.isEmpty()) {
                                applyMove(changes)
                            }
                        }
                }
            }
        }
    }

    private fun applyMove(changes: ChangesDTO) {
        currentPosition = changes.position

        chessboardView.applyStateChanges(changes)

        rotateBoardSideForSingleMode()
    }

    private fun rotateBoardSideForSingleMode() {
        if (gameMode == GameMode.SINGLE) {
            chessboardView.setSide(
                chessboardView.getState()?.side?.reverse(),
                SINGLE_MOVE_AUTO_ROTATION_ENABLED
            )
        }
    }

    private fun initChessboardContent() {
        if (!game.isConstructor) {
            Thread {
                networkService.gameApi.getChessboard(gameId, userId)
                    .enqueue { response ->
                        response.body()?.let {
                            if (!chessboardView.isInitialized()) {
                                chessboardView.init(it, side)

                                if (side != Side.nextTurnSide(it.position)) {
                                    // иначе не сможем сделать первый ход
                                    rotateBoardSideForSingleMode()
                                }
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

    //TODO: нужно добавить слушатель состояния activity в chessboardView
    override fun onResume() {
        super.onResume()
        chessboardView.onResume()
    }

    override fun onDestroy() {
        chessboardView.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        chessboardView.onPause()
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
            networkService.gameApi.rollback(gameId, userId, currentPosition, 1)
                .enqueue { response ->
                    response.body()?.let {
                        currentPosition = it.position
                        chessboardView.resetTo(it)
                    }
                }
        }.start()
    }

    /**
     * Отложенное появление панели конструтора доски
     */
    override fun onCellSizeChanged(cellSize: Int) {
        if (game.isConstructor) {
            @Suppress("PLUGIN_WARNING")
            chessboardConstructorBar.visibility = View.VISIBLE
        }
    }
}