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
    @Inject
    lateinit var networkService: INetworkService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chessboard)
        activityComponent.inject(this)

        ButterKnife.bind(this)
    }

    @OnClick(R.id.downloadChessboardButton)
    fun downloadChessboard() {
        println("downloadChessboard.start")
        Toast.makeText(this, "downloadChessboard", Toast.LENGTH_SHORT).show()

        networkService.debugApi.getChessboard()     //TODO: долго отправляется запрос в первый раз - можно это вынести из ui потока
            .enqueue(object : Callback<ChessboardDTO> {
                override fun onResponse(call: Call<ChessboardDTO>, response: Response<ChessboardDTO>) {
                    val chessboard = response.body()
                    println("success.body = $chessboard")

                    requireNotNull(chessboard)
                    chessboardView.update(chessboard)
                }

                override fun onFailure(call: Call<ChessboardDTO>, t: Throwable) {
                    printErr("failure.message = ${t.message}")
                    t.printStackTrace()
                }
            })

        println("downloadChessboard.end")
    }
}
