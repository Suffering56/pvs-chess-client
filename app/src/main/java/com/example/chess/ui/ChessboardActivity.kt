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
    private var chessboardState: ChessboardDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chessboard)
        activityComponent.inject(this)
        ButterKnife.bind(this)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        super.onSaveInstanceState(savedInstanceState)
        chessboardState?.let { savedInstanceState?.putSerializable(CHESSBOARD_STATE, it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        chessboardState = savedInstanceState?.get(CHESSBOARD_STATE) as ChessboardDTO?
        chessboardState?.let { chessboardView.update(it) }
    }

    @OnClick(R.id.rotateButton)
    fun rotateChessboard() {
        chessboardView.rotation += 180f
    }

    @OnClick(R.id.downloadChessboardButton)
    fun downloadChessboard() {
        println("downloadChessboard.start")
        Toast.makeText(this, "downloadChessboard", Toast.LENGTH_SHORT).show()

        Thread {
            //TODO: RxKotlin mb?
            networkService.debugApi.getChessboard()
                .enqueue(object : Callback<ChessboardDTO> {
                    override fun onResponse(call: Call<ChessboardDTO>, response: Response<ChessboardDTO>) {
                        val chessboard = response.body()
                        println("success.body = $chessboard")
                        requireNotNull(chessboard)
                        this@ChessboardActivity.chessboardState = chessboard

                        this@ChessboardActivity.runOnUiThread {
                            chessboardView.update(chessboard)
                        }
                    }

                    override fun onFailure(call: Call<ChessboardDTO>, t: Throwable) {
                        printErr("failure.message = ${t.message}")
                        t.printStackTrace()
                    }
                })
        }.start()

        println("downloadChessboard.end")
    }
}