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
        const val GAME = "gameId"
    }

    @Inject
    lateinit var networkService: INetworkService

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        activityComponent.inject(this)
        ButterKnife.bind(this)

        val playerId = getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @OnClick(R.id.newGameButton)
    fun createNewGame() {
        progressBar.visibility = View.VISIBLE

        Thread {
            networkService.initApi.createGame()
                .enqueue {
                    progressBar.visibility = View.INVISIBLE

                    val game = it.body()!!

                    val intent = Intent(this@MainActivity, ChessboardActivity::class.java)
                    intent.putExtra(GAME, game)
                    startActivity(intent)
                }
        }.start()    //first service call is too long
    }

    @OnClick(R.id.continueGameButton)
    fun continueGame() {
        progressBar.visibility = View.VISIBLE
        Thread {
            networkService.initApi.getGame(continueGameIdText.getTextAsLong())
                .enqueue {
                    progressBar.visibility = View.INVISIBLE

                    val intent = Intent(this@MainActivity, ChessboardActivity::class.java)
                    intent.putExtra(GAME, it.body()!!)
                    startActivity(intent)
                }
        }.start()   //first service call is too long
    }

    @OnTextChanged(R.id.continueGameIdText)
    fun onGameIdChanged(actualText: CharSequence) {
        continueGameButton.isEnabled = actualText.isNotEmpty()
    }
}

@SuppressLint("HardwareIds")
fun getUserId(context: Context) = getString(context.contentResolver, Settings.Secure.ANDROID_ID)

