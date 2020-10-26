package com.example.booksworld.main.search_suggestiosn_package

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import java.lang.IllegalArgumentException

class ContentPropvider_Suggestions: ContentProvider() {
    lateinit  var db: SQLiteDatabase
    lateinit var da :SuggestionsDataAdapter
    companion object{

        val NAME = "com.example.booksworld.main.search_suggestiosn_package.ContentPropvider_Suggestions"
        val CONTENT ="content://$NAME/${SeriesListDataBase.TABLE_NAME}"
        val CONTENT_URI= Uri.parse(CONTENT)

        val STUDENTS_PROJECTION_MAP = HashMap<String,String>()
        val RANDOM_CATEGORY="RANDOM_CATEGORY"
        var RANDOM_URI_KEY= 3
        var RANDOM_URI = Uri.withAppendedPath(CONTENT_URI, RANDOM_CATEGORY)
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(NAME, RANDOM_URI.path, RANDOM_URI_KEY)


        }
    }
    interface getCategory{
        companion object{

        }
    }
    override fun insert(uri: Uri, values: ContentValues?): Uri? {

       var long =  db.insert(SeriesListDataBase.TABLE_NAME,null, values)
        if(long>0){
            context!!.contentResolver.notifyChange(uri,null)
            println("insert:${long}")
            return uri
        }

        throw SQLException("SQL Exception: ${uri}")
    }
    /*

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {



        //var c = qb.query(db,projection,selection,selectionArgs,null,null,sortOrder)
        println("selection is:$selection, selection args:${selectionArgs!![0]}")

        var c = db.query(SeriesListDataBase.TABLE_NAME,SuggestionsDataAdapter.cols,selection,selectionArgs,null,null,sortOrder,"5")

        c.setNotificationUri(context!!.contentResolver,uri)
        return c
    }
*/
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?

    ): Cursor? {
        println("uri is:$uri")
        println("content is:$selection selectionArgs:${selectionArgs?.get(0)} sortOrder:${sortOrder}")
        when(uriMatcher.match(uri)){
            RANDOM_URI_KEY-> {
          var c=   da.getRandomBooksByTagsCursor(selectionArgs!!.get(0),7)
           // var splist = SuggestionsDataAdapter.getSeriesPropertiesListFromCursor(c)
             //   println("splist size is:${splist.size}")
            return c
        }

        }

        /*

        while(c.moveToNext()){
            var uid = c.getInt(c.getColumnIndex(SeriesListDataBase.UID))
            var name = c.getString(c.getColumnIndex(SeriesListDataBase.NAME))
            var extra = c.getString(c.getColumnIndex(SeriesListDataBase.SUGGEST_COLUMN_INTENT_DATA))
            println("uid:$uid name:${name} extra:${extra}")

        }
        c.moveToFirst()
        c.setNotificationUri(context!!.contentResolver,uri)
        */
        println("uri matcher is ${uriMatcher.match(uri)}")
        return getSuggestionsSeartcCursor(uri)
    }




    fun getSuggestionsSeartcCursor(uri:Uri):Cursor{
        var c =
            db.query(
                SeriesListDataBase.TABLE_NAME,
                SuggestionsDataAdapter.cols,
                SeriesListDataBase.NAME + " LIKE ?",
                arrayOf("%" + uri.lastPathSegment.toString() + "%"),
                null,
                null,
                null,
                "5"
            )

        return c
    }


    override fun onCreate(): Boolean {
        db = SeriesListDataBase(context!!).writableDatabase
        da = SuggestionsDataAdapter(context!!)
        return true

    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        var count = db.update(SeriesListDataBase.TABLE_NAME,values,selection,selectionArgs)
        context!!.contentResolver.notifyChange(uri,null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        var count = db.delete(SeriesListDataBase.TABLE_NAME,selection,selectionArgs)
        context!!.contentResolver.notifyChange(uri,null)
        return count
    }


    override fun getType(uri: Uri): String? {
        when(uriMatcher.match(uri)){
            RANDOM_URI_KEY  ->{
                return  ""
            }
           else->{
                return "";
            }
        }
        throw IllegalArgumentException("IllegalArgumentExeptions: ${uri}")
    }
}