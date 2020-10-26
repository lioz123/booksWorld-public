package com.example.booksworld.main.DownloadRecivers

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.adapters.CategoryPresentaionRecycleViewAdapter

class CategoryPresentationRecycleViewDownloadReciver(context: Context, var adapter:CategoryPresentaionRecycleViewAdapter, handler: Handler?) : DownloadReciver(context,handler){

     override fun onSuccess(resultData: Bundle){

         val name =resultData.getString(SeriesProperties.NAME)
         val uid =resultData.getInt(SeriesProperties.UID)

         Handler(context.mainLooper).post {
             adapter.update(uid)
             Toast.makeText(context,"added book:$name",Toast.LENGTH_LONG).show()
         }
        }

    override fun onFailed(resultData: Bundle) {
        val name = resultData[SeriesProperties.NAME] as String?
        printMessage("failed to download book:$name")
    }



}