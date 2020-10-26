package com.example.booksworld.main.starter_database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.booksworld.main.PropertiesObjects.SeriesProperties

class StarterDataAdapter {
    var helper: StarterDataHelper
    var context: Context
    val cols = arrayOf(
        StarterDataHelper.UID,
        StarterDataHelper.NAME,
        StarterDataHelper.URL,
        StarterDataHelper.AUTHOR,
        StarterDataHelper.TAGS,
        StarterDataHelper.IMG_SRC,
        StarterDataHelper.DESCRIPTION,
        StarterDataHelper.SUGGEST_COLUMN_INTENT_DATA,
        StarterDataHelper.SCROLLX,
        StarterDataHelper.SCROLLY,
        StarterDataHelper.SCALEX,
        StarterDataHelper.SCALEY,
        StarterDataHelper.PAGE,
        StarterDataHelper.ACTIVITY_NAME,
        StarterDataHelper.SP_UID
    )

    constructor(c: Context){
        helper= StarterDataHelper(c)
        context=c;
    }


    fun update(sp: SeriesProperties){
        println("book loaded:${sp.name} page:${sp.page} scrollX:${sp.scrollX} scrolY:${sp.scrolly} activity:${sp.activity}" )

        var where = "${StarterDataHelper.UID} = ?"
        var db= helper.writableDatabase
        var long =  db.update(StarterDataHelper.TABLE_NAME,getContentValues(sp),null,null)

        println("Update: $long")
    }



    private fun getSeriesPropertiesListFromCursor(c: Cursor):ArrayList<SeriesProperties> {
        var splist = ArrayList<SeriesProperties>()
        while (c.moveToNext()) {
            var name = c.getString(c.getColumnIndex(StarterDataHelper.NAME))
            var uid = c.getInt(c.getColumnIndex(StarterDataHelper.SP_UID))
            var author = c.getString(c.getColumnIndex(StarterDataHelper.AUTHOR))
            var tags = c.getString(c.getColumnIndex(StarterDataHelper.TAGS))
            var imgSrc = c.getString(c.getColumnIndex(StarterDataHelper.IMG_SRC))
            var description = c.getString(c.getColumnIndex(StarterDataHelper.DESCRIPTION))
            var url = c.getString(c.getColumnIndex(StarterDataHelper.URL))
            var raw = c.getInt(c.getColumnIndex(StarterDataHelper.SUGGEST_COLUMN_INTENT_DATA))
            var scaleX =c.getFloat(c.getColumnIndex(StarterDataHelper.SCALEX))
            var scaleY =c.getFloat(c.getColumnIndex(StarterDataHelper.SCALEY))
            var activity = c.getString(c.getColumnIndex(StarterDataHelper.ACTIVITY_NAME))
            var scrollX = c.getInt(c.getColumnIndex(StarterDataHelper.SCROLLX))
            var scrollY = c.getInt(c.getColumnIndex(StarterDataHelper.SCROLLY))
            var page = c.getInt(c.getColumnIndex(StarterDataHelper.PAGE))
            var sp = SeriesProperties(uid, name, url, tags, author, imgSrc, description).apply {
                this.raw = raw

                this.scrollX = scrollX
                this.scrolly = scrollY
                this.page=page
                this.activity=activity
            }
            splist.add(sp)
        }
        return splist
    }


    fun insert( sp:SeriesProperties) {
        println("start inserting")

        var db = helper.writableDatabase

                var values =getContentValues(sp)
                val long = db.insert(StarterDataHelper.TABLE_NAME,null,values)
                println("adding :${long}")

            }








    private fun getContentValues(sp: SeriesProperties): ContentValues {
        var values = ContentValues()
        values.put(StarterDataHelper.NAME,sp.name)
        values.put(StarterDataHelper.URL,sp.url)
        values.put(StarterDataHelper.TAGS,sp.tags)
        values.put(StarterDataHelper.AUTHOR,sp.author)
        values.put(StarterDataHelper.IMG_SRC,sp.imgSRC)
        values.put(StarterDataHelper.DESCRIPTION,sp.description)
        values.put(StarterDataHelper.SUGGEST_COLUMN_INTENT_DATA,sp.raw)
        values.put(StarterDataHelper.SCROLLX,sp.scrollX)
        values.put(StarterDataHelper.SCROLLY,sp.scrolly)

        values.put(StarterDataHelper.PAGE,sp.page)
        values.put(StarterDataHelper.ACTIVITY_NAME,sp.activity)
        values.put(StarterDataHelper.SP_UID,sp.uid)

        return values


    }

    fun getActivityState() : SeriesProperties {
        var db = helper.writableDatabase
        var c= db.query(StarterDataHelper.TABLE_NAME,cols,null, null,null,null,null)
        var splist = getSeriesPropertiesListFromCursor(c)
        println("splist size:${splist.size}")
        return if (splist.isEmpty()) SeriesProperties.getEmptyBuild() else splist[0]
    }

}