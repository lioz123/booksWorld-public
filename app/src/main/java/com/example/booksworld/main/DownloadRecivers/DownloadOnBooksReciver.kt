package com.example.booksworld.main.DownloadRecivers

import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import com.example.booksworld.main.CustomUtils.SearchReciver
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import org.json.JSONArray

 class DownloadOnBooksReciver(var reciver: SearchReciver, handler: Handler): ResultReceiver(handler) {
    companion object{
        val BOOK_SUCCEES_RESULT =1
        val SUCCEES_RESULT=2
        val  IS_FINISHED="isFinished"
        val JSON_ARR="JSON_ARR"
    }
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        super.onReceiveResult(resultCode, resultData)
                when(resultCode){
                    BOOK_SUCCEES_RESULT ->FinishedSearch(resultData)
                    else->{
                        reciver.send(resultCode,resultData)
                    }
                }

    }
    private fun SEND_RESULT(){}

    private fun FinishedSearch(resultData: Bundle?) {
        var splist = ArrayList<SeriesProperties>()
        var jsonArr = JSONArray(resultData?.getString(JSON_ARR)!!)
        for (i in 0 until jsonArr.length()) {
            var js = jsonArr.getJSONObject(i)
            splist.add(SeriesProperties.BuildSeriesPropertiesFromJSON(js))

        }
        reciver.reciveSearchResult(splist)
    }
}