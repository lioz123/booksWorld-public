package com.example.booksworld.main.CustomUtils

import android.os.StrictMode
import com.example.booksworld.BuildConfig

class StrictModeHelper {
    companion object{
        fun EnableStrictMode(){
            if(BuildConfig.DEBUG){
               var policy = StrictMode.ThreadPolicy.Builder().let {
                   it.detectAll()
                   it.penaltyLog()
                    it.build()
               }
            }
        }
    }
}