package com.example.booksworld.main.search_suggestiosn_package

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Exception

class SeriesListDataBase : SQLiteOpenHelper {

    companion object{
        val DATA_BASE_NAME="SERIES_LIST_DATABASE18"
        val VERSION=18
        val TABLE_NAME="SERIES_LIST_TABLE55"
        val NAME="suggest_text_1"
        val IMG_SRC="IMG_SRC"
        val UID="_id"
         val TAG="SeriesListDataBase"
        val PAGE="PAGE"
        val SCROLLY="SCROOLY"
        val URL ="URL_TEST"
        val CREATE_TABLE="CREATE TABLE ${TABLE_NAME} " +
                " (${UID} INTEGER PRIMARY KEY AUTOINCREMENT,${NAME} TEXT,$URL TEXT,${IMG_SRC} TEXT, ${SCROLLY} INTEGER,${PAGE} INTEGER);"
        val DROP_TABLE= "DROP TABLE IF EXISTS ${TABLE_NAME}"

    }
    var c:Context
    constructor( c:Context):super(c,
        DATA_BASE_NAME,null,
        VERSION
    ){
        this.c=c
    }
    override fun onCreate(db: SQLiteDatabase?) {
        print("there is a broblem with on create")
        if(db==null){
            print("db is null")
        }
        try {

            db?.execSQL(CREATE_TABLE)

            print("manage to create table:$CREATE_TABLE")
        }catch (e:Exception){
            print("did not able to create data base")
            e.printStackTrace()
        }
    }


    fun print(str:String){
        Log.d(TAG,str)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try{
            db?.execSQL(DROP_TABLE)
            onCreate(db)
        }catch (e:Exception){

        }

    }

}