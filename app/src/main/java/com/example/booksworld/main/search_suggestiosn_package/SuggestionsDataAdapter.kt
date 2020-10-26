package com.example.booksworld.main.search_suggestiosn_package

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.booksworld.main.CustomUtils.FileHelper
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import java.lang.Exception
import kotlin.collections.ArrayList

class SuggestionsDataAdapter {
    lateinit var db:SQLiteDatabase
    companion object{
        val cols = arrayOf(SeriesListDataBase.UID,SeriesListDataBase.NAME,SeriesListDataBase.URL,SeriesListDataBase.IMG_SRC,SeriesListDataBase.SCROLLY,SeriesListDataBase.PAGE)

        fun getContentValues(sp:SeriesProperties):ContentValues{
            var values = ContentValues()
            values.put(SeriesListDataBase.NAME,sp.name)
            values.put(SeriesListDataBase.IMG_SRC,sp.imgSRC)
            values.put(SeriesListDataBase.SCROLLY,sp.scrolly)
            println("inserting series:${sp.getString()}")
            values.put(SeriesListDataBase.URL,sp.url)
            println("update page to ${sp.page}")
            values.put(SeriesListDataBase.PAGE,sp.page)
            return values

        }
        fun getSeriesPropertiesListFromCursor(c:Cursor):ArrayList<SeriesProperties> {
            var splist = ArrayList<SeriesProperties>()
            if(c==null||c.isLast){
                return  splist
            }
            while (c.moveToNext()) {
                val name = c.getString(c.getColumnIndex(SeriesListDataBase.NAME))
                val uid = c.getInt(c.getColumnIndex(SeriesListDataBase.UID))
                val imgSrc = c.getString(c.getColumnIndex(SeriesListDataBase.IMG_SRC))
                val scrollY = c.getInt(c.getColumnIndex(SeriesListDataBase.SCROLLY))
                val page = c.getInt(c.getColumnIndex(SeriesListDataBase.PAGE))
                println("the url test is :${SeriesListDataBase.URL}")

                val url = c.getString(c.getColumnIndex(SeriesListDataBase.URL))
                val sp = SeriesProperties.BuildSmallSeriesProperties(uid, name, imgSrc).apply {
                    this.scrolly = scrollY
                    this.page=page
                    this.url=url
                }
                splist.add(sp)
                if(c.isLast){
                    return  splist
                }
            }
            return splist
        }
    }
    var helper:SeriesListDataBase
    var context:Context

    constructor(c:Context){
        helper=SeriesListDataBase(c)
        context=c;
        db= helper.writableDatabase
    }
    fun querySelected(str:String,limit:String? =null): Cursor {
        val db = helper.writableDatabase

         val cursor = db.query(SeriesListDataBase.TABLE_NAME,cols,
             SeriesListDataBase.NAME + " LIKE ?", arrayOf("%" + str + "%"),
             null, null, null,limit);

         return cursor
     }

    fun delete(sp:SeriesProperties){
        var db= helper.writableDatabase
        var where = "${SeriesListDataBase.UID} = ?"
        db.delete(SeriesListDataBase.TABLE_NAME,where, arrayOf("${sp.uid}"))
        FileHelper.delete(context,sp)
    }

    fun update(sp:SeriesProperties){
        println("book loaded uid:${sp.uid} ${sp.name} url:${sp.url} page:${sp.page} scrollX:${sp.scrollX} scrolY:${sp.scrolly}" )

        var where = "${SeriesListDataBase.UID} = ?"
        var db= helper.writableDatabase
       var long =  db.update(SeriesListDataBase.TABLE_NAME,getContentValues(sp),where, arrayOf("${sp.uid}"))
       var noSp= getBook(sp.uid)
        println("updated:${noSp.getString()}")
        println("Update: $long")
    }

    fun getSearchResultsForSelectedWord(str:String,limit:Int=10):ArrayList<SeriesProperties>{
        return getSeriesPropertiesListFromCursor(querySelected(str,limit.toString()))
     }



    fun getQueryTagThroughCursor(tag:String,limit:Int?):Cursor{
        var db = helper.writableDatabase
        var limitStr = if(limit==null)  null else "$limit"

        var c = db.query(SeriesListDataBase.TABLE_NAME, cols,
            SeriesListDataBase.NAME + " LIKE ?", arrayOf("%" + tag + "%"),
            null, null, "random()",limitStr)
        return c

    }

     fun getRandomBooksByTags(tags:String, limit: Int?):ArrayList<SeriesProperties>{
         var db = helper.readableDatabase


         var limitStr = if(limit==null)  null else "$limit"
         var c = db.query(SeriesListDataBase.TABLE_NAME, cols,
             SeriesListDataBase.NAME + " LIKE ?", arrayOf("%" + tags + "%"),
             null, null, "random()",limitStr)
       var splist=      getSeriesPropertiesListFromCursor(c)
            return splist
     }
    @Synchronized
    fun getRandomBooksByTagsCursor(tags:String, limit: Int?):Cursor{
        var db = helper.readableDatabase


        var limitStr = if(limit==null)  null else "$limit"
        var c = db.query(SeriesListDataBase.TABLE_NAME, cols,
            SeriesListDataBase.NAME + " LIKE ?", arrayOf("%" + tags + "%"),
            null, null, "random()",limitStr)

        return c
    }
    /*
     fun getBookByRawCount(str: String):SeriesProperties{
         var db = helper.writableDatabase
        println("getBookByRawCount:$str")

         var whereArgs =SeriesListDataBase.SUGGEST_COLUMN_INTENT_DATA  + " = ?"

       var c= db.query(SeriesListDataBase.TABLE_NAME,cols,whereArgs, arrayOf(str),null,null,null,null)
         var splist = getSeriesPropertiesListFromCursor(c)
         if(splist.size>0){
             return splist[0]
         }
         return SeriesProperties(-1,"","","","","","")
     }
     */
     fun insert( splist: List<SeriesProperties>) {
         println("start inserting")

         var db = helper.writableDatabase
         try{
             db.beginTransaction()

             splist.forEachIndexed {index , it ->
                 it.raw=index
                 var values =getContentValues(it)
                 val long = db.insert(SeriesListDataBase.TABLE_NAME,null,values)
                 println("adding :${long}")

             }
             db.setTransactionSuccessful();

         }catch (ex:Exception){

         }finally {
             db.endTransaction()
             db.close()

         }
    }



    fun getBook(uid: Int) :SeriesProperties{
        val where = "${SeriesListDataBase.UID} = ?"
        val db = helper.writableDatabase
        val c= db.query(SeriesListDataBase.TABLE_NAME,cols,where, arrayOf("$uid"),null,null,null)
        val splist = getSeriesPropertiesListFromCursor(c)
        return if (splist.isEmpty()) SeriesProperties.getEmptyBuild() else splist[0]
    }
    fun getBook(name:String): SeriesProperties {
        val where = "${SeriesListDataBase.NAME} = ?"
        val db = helper.writableDatabase
        val c= db.query(SeriesListDataBase.TABLE_NAME,cols,where, arrayOf(name),null,null,null,"1")
        val splist = getSeriesPropertiesListFromCursor(c)
        return if (splist.isEmpty()) SeriesProperties.getEmptyBuild() else splist[0]

    }
    fun getAllBooks(): ArrayList<SeriesProperties> {
        val db = helper.writableDatabase
        val c = db.query(SeriesListDataBase.TABLE_NAME,cols,null,null,null,null,null,null)
        return getSeriesPropertiesListFromCursor(c)
    }

    fun insert(sp: SeriesProperties) {
        val db= helper.writableDatabase
        val values = getContentValues(sp)
     db.insert(SeriesListDataBase.TABLE_NAME,null,values)

    }

    fun getFromUidList(list: MutableSet<String>?): ArrayList<SeriesProperties> {
        var splist =ArrayList<SeriesProperties>()
        val TAG= "SuggDataAdapter.class"
        Log.e(TAG,"list is:$list")
        if(list.isNullOrEmpty()){
            return  splist
        }
        var neoList= list as MutableSet<Long>

        neoList?.let {set->
                set.forEach {
                    println("getting series for uid:${it} series is ${getBook(it.toInt()+1).getString()}")
                    splist.add(getBook(it.toLong().toInt()+1))
                }
        }
        return splist
    }

}