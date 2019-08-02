package com.example.chess.ui

import android.os.Bundle
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.chess.R
import com.example.chess.network.INetworkService
import com.example.chess.printErr
import com.example.chess.shared.dto.ChessboardDTO
import kotlinx.android.synthetic.main.activity_chessboard.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class ChessboardActivity : BaseActivity() {

    companion object {
        private const val CHESSBOARD_STATE = "CHESSBOARD_STATE"
    }

    @Inject
    lateinit var networkService: INetworkService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chessboard)
        activityComponent.inject(this)
        ButterKnife.bind(this)

        Thread {
            networkService.debugApi.getChessboard()
                .enqueue(object : Callback<ChessboardDTO> {
                    override fun onResponse(call: Call<ChessboardDTO>, response: Response<ChessboardDTO>) {
                        response.body()?.let {
                            this@ChessboardActivity.runOnUiThread {
                                if (!chessboardView.isInitialized()) {
                                    chessboardView.init(it)
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<ChessboardDTO>, e: Throwable) {
                        printErr(e)
                    }
                })
        }.start()   //TODO: RxKotlin mb?
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
        chessboardView.setSide(chessboardView.getState()!!.side.reverse())
    }

    @OnClick(R.id.downloadChessboardButton)
    fun downloadChessboard() {
        println("downloadChessboard.start")
        Toast.makeText(this, "downloadChessboard", Toast.LENGTH_SHORT).show()
        println("downloadChessboard.end")
    }
}