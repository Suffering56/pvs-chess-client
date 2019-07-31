package com.example.chess.ui

import android.app.Activity
import android.os.Bundle
import com.example.chess.App
import com.example.chess.R
import com.example.chess.di.component.ActivityComponent
import com.example.chess.di.component.DaggerActivityComponent
import com.example.chess.di.module.ActivityModule

/**
 * @author v.peschaniy
 *      Date: 31.07.2019
 */
abstract class BaseActivity : Activity() {

    protected lateinit var activityComponent: ActivityComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityComponent = DaggerActivityComponent.builder()
            .activityModule(ActivityModule(this))
            .applicationComponent((applicationContext as App).applicationComponent)
            .build()
    }




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