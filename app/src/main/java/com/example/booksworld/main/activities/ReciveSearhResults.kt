package com.example.booksworld.main.activities

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booksworld.R
import com.example.booksworld.main.CustomUtils.SearchReciver
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.adapters.BookContentRecycleView
import com.example.booksworld.main.adapters.Explore_RecycleViewAdapter
import com.example.booksworld.main.search_suggestiosn_package.ContentPropvider_Suggestions
import com.example.booksworld.main.search_suggestiosn_package.SeriesListDataBase
import com.example.booksworld.main.search_suggestiosn_package.SuggestionsDataAdapter
import com.google.android.material.appbar.MaterialToolbar

class ReciveSearhResults : AppCompatActivity(),SearchReciver{
    lateinit var mScrollView :RecyclerView
    lateinit var mToolbar: MaterialToolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reacive_search_result)
        mScrollView=findViewById(R.id.ativity_reacive_search_scrollView)
        mToolbar= findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        handleIntent(intent)
    }

/*
    fun QuerySearch(str: String, skip: Int, limit: Int){
        Handler(Looper.getMainLooper()).post {
            var intent = Intent(this, DownloadService::class.java).apply {
                action = DownloadService.SEARCH_BOOKS_ACTION
                putExtra(DownloadService.SEARCH_RESULT,str)
                putExtra(
                    DownloadService.DOWNLOAD_RECIVER,
                    DownloadReciver(this@ReciveSearhResults, Handler())
                )
                putExtra(ServerHelper.LIMIT,limit)
                putExtra(ServerHelper.SKIP,skip)
            }
            startService(intent)
        }

    }
    */

    fun QuerySearch(str:String,limit: Int){
        val da = SuggestionsDataAdapter(this)
        reciveSearchResult(  da.getSearchResultsForSelectedWord(str,limit))
    }



    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        mToolbar.inflateMenu(R.menu.search_bar_menu)
        val sm = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenu= menu!!.findItem(R.id.search_bar_menu)!!.actionView as SearchView
        searchMenu.setSearchableInfo( sm.getSearchableInfo(
            ComponentName(applicationContext,
                ReciveSearhResults::class.java)
        ))
        contentResolver.query(ContentPropvider_Suggestions.CONTENT_URI,null,null,null,null)
        //     loadItems("")
        val obj = getOnQueryTextListener()
        searchMenu.setOnQueryTextListener(obj)
        val suggestionListener = getOnSuggestionListener(searchMenu)
        searchMenu.setOnSuggestionListener(suggestionListener as SearchView.OnSuggestionListener)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun getOnSuggestionListener(searchMenu: SearchView): SearchView.OnSuggestionListener {
        return object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {

                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val c = searchMenu.suggestionsAdapter.cursor
                c.moveToPosition(position)
                var name = c.getString(c.getColumnIndex(SeriesListDataBase.NAME))
                var intent = Intent(applicationContext, ReciveSearhResults::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra(SearchManager.QUERY, name)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                applicationContext.startActivity(intent)
                return true
            }
        }
    }

    private fun getOnQueryTextListener(): SearchView.OnQueryTextListener {
        val obj = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val intent = Intent(
                    applicationContext,
                    ReciveSearhResults::class.java
                )
                intent.putExtra(SearchManager.QUERY, query)
                println("text submit")
                intent.action = Intent.ACTION_SEARCH
                startActivity(intent)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {


                return true
            }
        } as SearchView.OnQueryTextListener
        return obj
    }

    // Site: youtube
    fun handleIntent(intent:Intent){
        Thread(Runnable {
        println("intnet action= ${intent.action}")
        when(intent.action){
            Intent.ACTION_VIEW->{
                println("dataString:${intent.getStringExtra(SearchManager.QUERY)}")
                QuerySearch(intent.getStringExtra(SearchManager.QUERY)!!,1) //   splist.add(db.getBookByRawCount(intent.dataString!!))
            }
            Intent.ACTION_SEARCH->{
                val str =intent.getStringExtra(SearchManager.QUERY) as String
                QuerySearch(str,  10)
            }
            Explore_RecycleViewAdapter.ACTION_SPECIFIC_SP->{
                val sp = intent.getParcelableExtra<SeriesProperties>(SeriesProperties.SERIES_PROPERTIES_KEY)
                QuerySearch(sp.name!!.toLowerCase(),  1)
                println("has image loaded:${sp.name } , loaded:${sp.imageloaded}")
            }
        }

        }).start()
    }

    override fun reciveSearchResult(splist: ArrayList<SeriesProperties>) {
        val progressBar = findViewById<ProgressBar>(R.id.activity_receive_result_progressBar)
        progressBar.post {
            progressBar.visibility= View.GONE
        }
        val viewAdapter = BookContentRecycleView(splist)
        val viewMannager = LinearLayoutManager(this)
        viewMannager.orientation=RecyclerView.VERTICAL
        mScrollView.post {
            mScrollView.apply {
                adapter=viewAdapter
                layoutManager=viewMannager
                setHasFixedSize(true)
            }
        }

    }

    override fun send(resultCode: Int, resultData: Bundle?) {

    }


}
