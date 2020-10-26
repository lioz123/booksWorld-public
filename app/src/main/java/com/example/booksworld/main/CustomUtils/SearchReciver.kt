package com.example.booksworld.main.CustomUtils

import android.os.Bundle
import com.example.booksworld.main.PropertiesObjects.SeriesProperties

interface SearchReciver{
    fun reciveSearchResult(splist: ArrayList<SeriesProperties>)
     fun send(resultCode: Int, resultData: Bundle?)
}