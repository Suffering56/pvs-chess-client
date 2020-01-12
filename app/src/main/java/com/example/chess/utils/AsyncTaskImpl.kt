package com.example.chess.utils

import android.os.AsyncTask

class AsyncTaskImpl<Params, Progress, Result>(
    private val backgroundAction: (Array<out Params>) -> Result
) : AsyncTask<Params, Progress, Result>() {

    override fun doInBackground(vararg params: Params): Result {
        return backgroundAction.invoke(params)
    }
}