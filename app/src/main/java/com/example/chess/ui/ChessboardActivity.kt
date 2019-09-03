package com.example.chess.ui

import android.os.Bundle
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.chess.R
import com.example.chess.network.INetworkService
import com.example.chess.shared.dto.ChangesDTO
import com.example.chess.shared.dto.GameDTO
import com.example.chess.shared.dto.MoveDTO
import com.example.chess.shared.dto.PointDTO
import com.example.chess.shared.enums.Side
import com.example.chess.utils.enqueue
import kotlinx.android.synthetic.main.chessboard_activity.*
import javax.inject.Inject


class ChessboardActivity : BaseActivity() {

    companion object {
        private const val CHESSBOARD_STATE = "CHESSBOARD_STATE"
    }

    @Inject
    lateinit var networkService: INetworkService

    private val getAvailableMovesListener: (Int, Int) -> Set<PointDTO> = { _, _ ->
        setOf(
            PointDTO(0, 0),
            PointDTO(4, 3),
            PointDTO(4, 4),
            PointDTO(4, 5),
            PointDTO(4, 6),
            PointDTO(5, 2)
        )
    }

    private val applyMoveListener: (MoveDTO) -> ChangesDTO = { move ->
        ChangesDTO(chessboardView.getState()!!.position + 1, move, PointDTO(7, 3))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chessboard_activity)
        activityComponent.inject(this)
        ButterKnife.bind(this)

        val game = intent.getSerializableExtra(MainActivity.GAME) as GameDTO
        val side = intent.getSerializableExtra(MainActivity.SIDE) as Side?
        val userId = intent.getSerializableExtra(MainActivity.USER_ID) as String

        chessboardView.getAvailableMovesListener = { rowIndex, columnIndex ->
            networkService.gameApi.getAvailableMoves(userId, game.id, rowIndex, columnIndex)
        }
//        chessboardView.getAvailableMovesListener = getAvailableMovesListener
        chessboardView.applyMoveListener = applyMoveListener

        Thread {
            networkService.debugApi.getChessboard()
                .enqueue { response ->
                    response.body()?.let {
                        if (!chessboardView.isInitialized()) {
                            chessboardView.init(it, side)
                        }
                    }
                }
        }.start()   //first service call is too long
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        super.onSaveInstanceState(savedInstanceState)
        chessboardView.getState()?.let { savedInstanceState?.putSerializable(CHESSBOARD_STATE, it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        val chessboardState = savedInstanceState?.get(CHESSBOARD_STATE) as ChessboardView.State?
        chessboardState?.let { chessboardView.init(it) }
    }

    @OnClick(R.id.rotateButton)
    fun rotateChessboard() {
        chessboardView.getState()!!.side?.let { chessboardView.setSide(it.reverse()) }
    }
}