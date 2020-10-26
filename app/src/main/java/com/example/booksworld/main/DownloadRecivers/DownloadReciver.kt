package com.example.booksworld.main.DownloadRecivers

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.widget.Toast

abstract class  DownloadReciver(val context: Context, handler: Handler?) : ResultReceiver(handler) {
    companion object{
        val SUCCESS_CODE=1
        val FAILED_CODE=2
        val TAG = "DownloadReciver.class"
    }


    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        super.onReceiveResult(resultCode, resultData)
        if(resultData==null){return}

        when(resultCode){
            SUCCESS_CODE->onSuccess(resultData)
            FAILED_CODE->onFailed(resultData)
        }
    }
     fun printMessage(message: String) {

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    }

    abstract fun onSuccess(resultData: Bundle)
    abstract fun onFailed(resultData: Bundle)
}