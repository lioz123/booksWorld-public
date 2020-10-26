package com.example.booksworld.main.starter_database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.Exception

class StarterDataHelper: SQLiteOpenHelper {
    companion object{
        val TABLE_NAME="STARTER_DATA_TABLE"
        val DATABASE_NAME="STARTER_DATABASE"
        val VERSION = 2
        val NAME =  "suggest_text_1"
        val AUTHOR="AUTHOR"
        // var name:String , var url:String,var tags :String , var authour:String,var imgSRC:String,var description:String
        val URL="URL"
        val TAGS="TAGS"
        val IMG_SRC="IMG_SRC"
        val DESCRIPTION ="DESCRIPTION"
        val COL_ICON="SUGGEST_COLUMN_ICON_1"
        //        val SUGGEST_COLUMN_ICON_2  SUGGEST_COLUMN_INTENT_ACTION SUGGEST_COLUMN_INTENT_DATA SUGGEST_COLUMN_INTENT_DATA_ID SUGGEST_COLUMN_INTENT_EXTRA_DATA SUGGEST_COLUMN_QUERY SUGGEST_COLUMN_SHORTCUT_ID SUGGEST_COLUMN_SPINNER_WHILE_REFRESHING
        val UID="_id"
        val SP_UID ="SERIES_UID"
        val SCALEX ="SCALEX"
        val SCALEY="SCALEY"
        val DOWNLOAD="DOWNLOADED"
        val PAGE ="PAGE"
        val SCROLLX= "SCROLLX"
        val SCROLLY="SCROLLY"
        val ACTIVITY_NAME="ACTIVITY_NAME"

        val SUGGEST_COLUMN_INTENT_DATA="suggest_intent_data"
        val CREATE_TABLE =  "CREATE TABLE ${TABLE_NAME}" +
                " (${UID} INTEGER PRIMARY KEY AUTOINCREMENT,$NAME TEXT,$URL TEXT,${TAGS} TEXT,$AUTHOR TEXT,$IMG_SRC TEXT,${DESCRIPTION} TEXT,${SUGGEST_COLUMN_INTENT_DATA} INTEGER,$SCROLLX INTEGER,$SCROLLY INTEGER,$SCALEX REAL,$SCALEY REAL,$DOWNLOAD INTEGER,$PAGE INTEGER,$ACTIVITY_NAME TEXT,$SP_UID INTEGER);"
        val DROP_TABLE= "DROP TABLE IF EXISTS $TABLE_NAME"
    }
    var c:Context
    constructor(c:Context):super(c,
        DATABASE_NAME,null,
      VERSION
    ){
        this.c=c

    }

    override fun onCreate(db: SQLiteDatabase?) {
        try{
            db?.execSQL(CREATE_TABLE)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try{
            db?.execSQL(DROP_TABLE)
            onCreate(db)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}