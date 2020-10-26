package com.example.booksworld.main.CustomUtils

import android.content.Context
import android.content.SharedPreferences
import com.example.booksworld.main.activities.Explore_Activity

class SharedPrefrencesUtils {
    companion object{
        val SHARED_PREFERNSES_DOCUMENT ="SHARED_PREFRENCES_DOCUMENT1"
        val LAST_ACTIVITY_KEY="LAST_ACTIVITY_KEY"

        val UID_KEY="UID_KEY"
        lateinit var sharedPrefrences:SharedPreferences
        fun INITIALIZE_SHARED_PREFRENCES(context:Context){
            sharedPrefrences = context.getSharedPreferences(SHARED_PREFERNSES_DOCUMENT,Context.MODE_PRIVATE)
        }

        fun update(map:HashMap<String,Any>){


            if(this::sharedPrefrences.isInitialized){
                var edit = sharedPrefrences.edit()
                map.keys.forEach {key->
                    when(map[key]){
                        is Int -> edit.putInt(key,map[key] as Int)
                        is String -> edit.putString(key,map[key] as String)
                    }
                }
                edit.commit()
            }
            println("shared prefrences does'nt exist")
        }

        fun getUid():Int{
            if(this::sharedPrefrences.isInitialized){
                return sharedPrefrences.getInt(UID_KEY,-1)
            }
            println("shared prefrences does'nt exist")
            return -1
        }

        fun getActivity():String{
            if(this::sharedPrefrences.isInitialized){
                return sharedPrefrences.getString(LAST_ACTIVITY_KEY , "") as String
            }
            return Explore_Activity::class.qualifiedName as String
        }
    }


}