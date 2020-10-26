package com.example.booksworld.main.DownloadRecivers

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.example.booksworld.main.PropertiesObjects.SeriesProperties

class BookContentDownloadReciver(context: Context, handler: Handler?) : DownloadReciver(context,handler) {


    override fun onSuccess(resultData: Bundle) {
        val name = resultData.getString(SeriesProperties.NAME)
        printMessage( "book added$name")
    }


    override fun onFailed(resultData: Bundle) {
        val name = resultData
        printMessage( "failed to download$name")
    }


}