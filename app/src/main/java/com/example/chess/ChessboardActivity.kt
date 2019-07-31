package com.example.chess

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chess.di.App
import com.example.chess.di.component.ActivityComponent
import com.example.chess.di.component.DaggerActivityComponent
import com.example.chess.di.module.ActivityModule
import com.example.chess.network.INetworkService
import javax.inject.Inject

class ChessboardActivity : AppCompatActivity() {
    @Inject
    lateinit var networkService: INetworkService

    private lateinit var activityComponent: ActivityComponent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chessboard)

        activityComponent = DaggerActivityComponent.builder()
            .activityModule(ActivityModule(this))
            .applicationComponent(App.get(this).applicationComponent)
            .build()

        activityComponent.inject(this)
        println("networkService22 = $networkService")
    }
}
