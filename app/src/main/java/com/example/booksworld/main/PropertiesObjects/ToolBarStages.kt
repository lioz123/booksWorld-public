package com.example.booksworld.main.PropertiesObjects

import java.lang.Exception

enum class ToolBarStages {
    ReadBrawser, Explore,none,Downloads ;
    companion object {
        fun getEnumFromString(str:String):ToolBarStages{
            try{
                return valueOf(str)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return none
        }
    }
}