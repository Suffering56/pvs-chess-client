package com.example.chess

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chess.di.App
import com.example.chess.di.component.ActivityComponent
import com.example.chess.di.component.DaggerActivityComponent
import com.example.chess.di.module.ActivityModule
import com.example.chess.network.INetworkService
import javax.inject.Inject


/**
 * @author v.peschaniy
 *      Date: 18.07.2019
 */
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var networkService: INetworkService
    private lateinit var activityComponent: ActivityComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        activityComponent = DaggerActivityComponent.builder()
            .activityModule(ActivityModule(this))
            .applicationComponent(App.get(this).applicationComponent)
            .build()

        activityComponent.inject(this)

        println("networkService111 = ${networkService}")
    }

    fun onButtonClick(view: View) {
        Toast.makeText(this, "on button click: start", Toast.LENGTH_LONG).show()

        val intent = Intent(this, ChessboardActivity::class.java)
        startActivity(intent)
//        println("networkService = ${networkService}")

//        networkService.debugApi
//            .getChessboard()
//            .enqueue(object : Callback<ChessboardDTO> {     //TODO: выглядит так словно GUI замирает в ожидании респонса. возможно RxJava все же нужен, ну или Handler
//                override fun onResponse(call: Call<ChessboardDTO>, response: Response<ChessboardDTO>) {
//                    val chessboard = response.body()
////                    chessboard?.matrix?.forEach {
////                        it.forEach(::println)
////                    }
//                    println("success.body = $chessboard")
//                }
//
//                override fun onFailure(call: Call<ChessboardDTO>, t: Throwable) {
//                    printErr("failure.message = ${t.message}")
//                    t.printStackTrace()
//                }
//            })
    }
}

fun printErr(msg: String) {
    System.err.println(msg)
}
