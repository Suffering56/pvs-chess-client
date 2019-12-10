package com.example.chess.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure.getString
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnTextChanged
import com.example.chess.R
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

    companion object {
        const val GAME = "game"
        const val SIDE = "side"
        const val USER_ID = "userId"
    }

    @Inject
    lateinit var networkService: INetworkService

    private lateinit var game: GameDTO

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        activityComponent.inject(this)
        ButterKnife.bind(this)
        //TODO: надо как-то победить долгий первый вызов ретрофита
        showNextStep()
    }

    override fun onResume() {
        super.onResume()
        showMainLayout()
        if (::game.isInitialized) {
            continueGameIdText.setText(game.id.toString())
        }
    }

    @OnClick(R.id.newGameButton)
    fun createNewGame() {
//        game = GameDTO(
//            1,
//            0,
//            GameMode.SINGLE,
//            Side.WHITE,
//            emptyList()
//        )
//        showNextStep()
//        return

        progressBar.visibility = View.VISIBLE

        Thread {
            networkService.initApi.createGame()
                .enqueue {
                    progressBar.visibility = View.INVISIBLE
                    game = it.body()!!
                    showNextStep()
                }
        }.start()    //first service call is too long
    }

    @OnClick(R.id.continueGameButton)
    fun continueGame() {
        progressBar.visibility = View.VISIBLE

        Thread {
            networkService.initApi.getGame(userId, continueGameIdText.getTextAsLong())
                .enqueue {
                    progressBar.visibility = View.INVISIBLE
                    game = it.body()!!
                    showNextStep()
                }
        }.start()   //first service call is too long
    }

    @OnClick(value = [R.id.singleModeButton, R.id.pvpModeButton, R.id.aiModeButton])
    fun selectGameModeClick(view: View) {

        val gameMode = when (view.id) {
            R.id.singleModeButton -> GameMode.SINGLE
            R.id.pvpModeButton -> GameMode.PVP
            R.id.aiModeButton -> GameMode.AI
            else -> throw UnsupportedOperationException()
        }

        networkService.initApi.setGameMode(userId, game.id, gameMode)
            .enqueue {
                game = it.body()!!
                showNextStep()
            }
    }

    @OnClick(value = [R.id.whiteSideButton, R.id.blackSideButton])
    fun selectSideButton(view: View) {

        val side = when (view.id) {
            R.id.whiteSideButton -> Side.WHITE
            R.id.blackSideButton -> Side.BLACK
            else -> throw UnsupportedOperationException()
        }

        networkService.initApi.setSide(userId, game.id, side)
            .enqueue {
                game = it.body()!!
                showNextStep()
            }
    }

    @OnTextChanged(R.id.continueGameIdText)
    fun onGameIdChanged(actualText: CharSequence) {
        continueGameButton.isEnabled = actualText.isNotEmpty()
    }

    private fun showNextStep() {
        if (!::game.isInitialized) {
            showMainLayout()
        } else {
            if (game.mode == GameMode.UNSELECTED) {
                showModeLayout()
            } else {
                val side = game.side

                if (side == null) {
                    if (game.freeSideSlots.isNotEmpty()) {
                        showSideLayout()
                    } else {
                        showChessboardActivity(null)    //isViewer
                    }
                } else {
                    showChessboardActivity(side)
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

    private fun showSideLayout() {
        whiteSideButton.isEnabled = false
        blackSideButton.isEnabled = false

        game.freeSideSlots.forEach {
            when (it) {
                Side.WHITE -> whiteSideButton.isEnabled = true
                Side.BLACK -> blackSideButton.isEnabled = true
            }
        }

        mainLayout.visibility = View.INVISIBLE
        chooseModeLayout.visibility = View.INVISIBLE

        chooseSideLayout.visibility = View.VISIBLE
    }

    private fun showChessboardActivity(side: Side?) {
        val intent = Intent(this, ChessboardActivity::class.java)
        intent.putExtra(GAME, game)
        intent.putExtra(SIDE, side)
        intent.putExtra(USER_ID, userId)
        startActivity(intent)
    }

    private val userId get() = getUserId(applicationContext)
}

@SuppressLint("HardwareIds")
fun getUserId(context: Context): String = getString(context.contentResolver, Settings.Secure.ANDROID_ID)

