package com.example.chess.ui

import android.content.Intent
import android.os.Bundle
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.chess.R

/**
 * @author v.peschaniy
 *      Date: 18.07.2019
 */
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityComponent.inject(this)

        ButterKnife.bind(this)
    }

    @OnClick(R.id.newGameButton)
    fun onButtonClick() {
        val intent = Intent(this, ChessboardActivity::class.java)
        startActivity(intent)
    }
}

