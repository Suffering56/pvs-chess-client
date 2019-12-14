package com.example.chess.utils

import com.example.chess.printErr
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun <T> Call<T>.enqueue(callback: (response: Response<T>) -> Unit) {
    this.enqueue(object : Callback<T> {

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.body() != null) {
                callback.invoke(response)
            } else {
                val errorString = response.errorBody()?.string()
                printErr("response body is null! errorBody: $errorString")
            }
        }

        override fun onFailure(call: Call<T>, e: Throwable) {
            printErr("onFailure: ${e.message}", e)
        }
    })
}