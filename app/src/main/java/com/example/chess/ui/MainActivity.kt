package com.example.chess.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTextChanged
import com.example.chess.App
import com.example.chess.GameState
import com.example.chess.R
import com.example.chess.getUserId
import com.example.chess.network.INetworkService
import com.example.chess.shared.dto.GameDTO
import com.example.chess.shared.enums.GameMode
import com.example.chess.shared.enums.Side
import com.example.chess.utils.enqueue
import com.example.chess.utils.getTextAsLong
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

/**
 * @author v.peschaniy
 *      Date: 18.07.2019
 */
class MainActivity : BaseActivity() {

    @Inject
    lateinit var networkService: INetworkService

    private lateinit var game: GameState

    private val userId get() = getUserId(applicationContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        activityComponent.inject(this)
        ButterKnife.bind(this)
        showNextStep()
    }

    override fun onResume() {
        super.onResume()
        showMainLayout()
        if (::game.isInitialized) {
            game.id?.let {
                continueGameIdText.setText(it.toString())
            }
        }
    }

    @OnClick(R.id.newGameButton)
    fun createNewGame() {
        game = GameState(userId, false)
        showNextStep()
    }

    @OnClick(R.id.newConstructorGameButton)
    fun createConstructorGame() {
        game = GameState(userId, true)
        showNextStep()
    }

    @OnClick(R.id.joinGameButton)
    fun joinGame() {
        progressBar.visibility = View.VISIBLE

        Thread {
            networkService.initApi.getGame(userId, continueGameIdText.getTextAsLong())
                .enqueue {
                    progressBar.visibility = View.INVISIBLE
                    game = GameState(userId, it.body()!!)
                    showNextStep()
                }
        }.start()
    }

    @OnClick(value = [R.id.singleModeButton, R.id.pvpModeButton, R.id.aiModeButton])
    fun selectGameModeClick(view: View) {
        game.mode = when (view.id) {
            R.id.singleModeButton -> GameMode.SINGLE
            R.id.pvpModeButton -> GameMode.PVP
            R.id.aiModeButton -> GameMode.AI
            else -> throw UnsupportedOperationException()
        }
        showNextStep()
    }

    @OnClick(value = [R.id.whiteSideButton, R.id.blackSideButton])
    fun selectSideButton(view: View) {
        val side = when (view.id) {
            R.id.whiteSideButton -> Side.WHITE
            R.id.blackSideButton -> Side.BLACK
            else -> throw UnsupportedOperationException()
        }
        if (game.isConstructor) {
            game.side = side
            showNextStep()
            return
        }

        if (game.isNew) {
            networkService.initApi.createGame(userId, game.mode, side)
                .enqueue {
                    val serverGame = it.body()!!
                    checkSide(serverGame, side)
                    game.side = side
                    game.id = serverGame.id
                    showNextStep()
                }
        } else {
            networkService.initApi.setSide(userId, game.id!!, side)
                .enqueue {
                    checkSide(it.body()!!, side)
                    game.side = side
                    showNextStep()
                }
        }
    }

    private fun checkSide(serverGame: GameDTO, side: Side) {
        require(serverGame.side == side) {
            "server side: ${serverGame.side} and client side: $side are not equals"
        }
    }

    @OnTextChanged(R.id.continueGameIdText)
    fun onGameIdChanged(actualText: CharSequence) {
        joinGameButton.isEnabled = actualText.isNotEmpty()
    }

    private fun showNextStep() {
        if (!::game.isInitialized) {
            showMainLayout()
        } else {
            if (game.mode == GameMode.UNSELECTED) {
                showModeLayout()
            } else {
                if (game.side == null) {
                    val freeSideSlots = game.getFreeSideSlots()

                    if (freeSideSlots.isEmpty()) {
                        // isViewer
                        showChessboardActivity()
                    } else {
                        showSideLayout(freeSideSlots)
                    }
                } else {
                    // side already selected
                    showChessboardActivity()
                }
            }
        }
    }

    private fun showMainLayout() {
        chooseModeLayout.visibility = View.INVISIBLE
        chooseSideLayout.visibility = View.INVISIBLE

        mainLayout.visibility = View.VISIBLE
    }

    private fun showModeLayout() {
        mainLayout.visibility = View.INVISIBLE
        chooseSideLayout.visibility = View.INVISIBLE

        chooseModeLayout.visibility = View.VISIBLE
    }

    private fun showSideLayout(freeSideSlots: Iterable<Side>) {
        whiteSideButton.isEnabled = false
        blackSideButton.isEnabled = false

        freeSideSlots.forEach {
            when (it) {
                Side.WHITE -> whiteSideButton.isEnabled = true
                Side.BLACK -> blackSideButton.isEnabled = true
            }
        }

        mainLayout.visibility = View.INVISIBLE
        chooseModeLayout.visibility = View.INVISIBLE

        chooseSideLayout.visibility = View.VISIBLE
    }

    private fun showChessboardActivity() {
        val intent = Intent(this, ChessboardActivity::class.java)
        intent.putExtra(App.EXTRAS_GAME, game)
        startActivity(intent)
    }
}
