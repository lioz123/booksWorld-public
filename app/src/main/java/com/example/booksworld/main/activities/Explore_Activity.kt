package com.example.booksworld.main.activities

import android.app.Activity
import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booksworld.R
import com.example.booksworld.main.CustomUtils.*
import com.example.booksworld.main.DownloadRecivers.DownloadOnBooksReciver
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.PropertiesObjects.SeriesRawProperties
import com.example.booksworld.main.Services.DownloadService
import com.example.booksworld.main.adapters.Explore_RecycleViewAdapter
import com.example.booksworld.main.search_suggestiosn_package.SeriesListDataBase
import com.example.booksworld.main.search_suggestiosn_package.SuggestionsDataAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.app_bar_layout.*

class Explore_Activity : AppCompatActivity(), NavigationDrawerProperties, SearchReciver {
lateinit var mToolbar: MaterialToolbar
    lateinit var mDrawerLayout:DrawerLayout
    lateinit var mNavigationview:NavigationView
    lateinit var recyclerView: RecyclerView
    val LOADER_CATEGORIES_ID = 0
    lateinit var mAdapter :Explore_RecycleViewAdapter
    lateinit var mSearchMenu:SearchView
    var finishedLoading =false

    companion object{
        fun InitializeRecyclerView(c:Context,recycler:RecyclerView,adapter : RecyclerView.Adapter<RecyclerView.ViewHolder?>?){
            var layoutManager = LinearLayoutManager(c)
                layoutManager.orientation=RecyclerView.VERTICAL
            recycler.post {
                recycler.apply {
                    this.layoutManager=layoutManager
                    setAdapter(adapter)
                    setHasFixedSize(true)
                }
            }

        }
        fun InitializeRecyclerViewHoriznotal(c:Context,recycler:RecyclerView,adapter : RecyclerView.Adapter<RecyclerView.ViewHolder?>?){
            var layoutManager = LinearLayoutManager(c)
            layoutManager.orientation=RecyclerView.HORIZONTAL

            recycler.apply {
                this.layoutManager=layoutManager
                this.adapter=adapter
                setHasFixedSize(true)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)
        InitRecycleView()
       // initLoaderMannager()

        mToolbar= findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mNavigationview= findViewById(R.id.navigation_layout)

        mToolbar.setNavigationOnClickListener {
            startActivity(Intent(applicationContext,
                Explore_Activity::class.java))
        }
   NavigationDrawerHelper.getInstance(this)

    }


    fun TestDownloadOnClick(v:View){
        var intent = Intent(this,DownloadService::class.java).apply {
            action = DownloadService.SEARCH_BOOKS_ACTION
            putExtra(DownloadService.SEARCH_RESULT,"the")
            putExtra(DownloadService.DOWNLOAD_RECIVER,
                DownloadOnBooksReciver(this@Explore_Activity,Handler())
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }else{
            startService(intent)
        }
    }

    fun InitRecycleView(){


        Thread(Runnable {
            recyclerView = findViewById(R.id.explore_recyle_view)
            val progressBar = findViewById<ProgressBar>(R.id.activity_receive_result_progressBar)

            mAdapter = Explore_RecycleViewAdapter(arrayListOf())


            var da = SuggestionsDataAdapter(this)
            var layoutManager = LinearLayoutManager(this)
            layoutManager.orientation=RecyclerView.VERTICAL
            recyclerView.post {
                recyclerView.apply {
                    this.layoutManager=layoutManager
                    this.adapter=mAdapter
                    setHasFixedSize(true)
                }
            }
            SeriesProperties.TAGS_LIST.forEach{
                    var splist = da.getRandomBooksByTags(it, 7)
               insertSeriesRawToAdapter(SeriesRawProperties(it,splist))

            }


            println("init srlist")
            progressBar.post {
                progressBar.visibility=View.GONE
            }


          //  InitializeRecyclerView(this,recyclerView,Explore_RecycleViewAdapter(srlist) as RecyclerView.Adapter<RecyclerView.ViewHolder?>)


            finishedLoading=true
        }).apply {
            start()
        }




    }
    @Synchronized
    fun insertSeriesRawToAdapter(sr:SeriesRawProperties){
        recyclerView.post {
            mAdapter.srlist.add(sr)
            mAdapter.notifyItemInserted(mAdapter.itemCount-1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        /*
               var da= StarterDataAdapter(this)
        var sp = da.getActivityState()
        sp.activity=Explore_Activity::class.qualifiedName as String
        da.update(sp)
         */

        var values = HashMap<String ,Any>()
        values.put(SharedPrefrencesUtils.LAST_ACTIVITY_KEY,Explore_Activity::class.qualifiedName as Any)
        SharedPrefrencesUtils.update(values)
            super.onPause()


    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        mToolbar.inflateMenu(R.menu.search_bar_menu)
        val sm = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenu= menu!!.findItem(R.id.search_bar_menu)!!.actionView as SearchView
        setSearchInfo(searchMenu, sm)
        setQueryListener(searchMenu)
        setSuggestionListener(searchMenu)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setSuggestionListener(searchMenu: SearchView) {
        val suggestionListener = object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                var c = searchMenu.suggestionsAdapter.cursor
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
        searchMenu.setOnSuggestionListener(suggestionListener as SearchView.OnSuggestionListener)
    }

    private fun setQueryListener(searchMenu: SearchView) {
        val obj = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                var intent = Intent(
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
                println("on query text change:$newText")
                return true
            }

        } as SearchView.OnQueryTextListener
        searchMenu.setOnQueryTextListener(obj)
    }

    /*
    private fun InitiateSearch(menu: Menu?) {
        mToolbar!!.inflateMenu(R.menu.search_bar_menu)


        var sm = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchMenu= menu!!.findItem(R.id.search_bar_menu)!!.actionView as SearchView
        setSearchInfo(searchMenu, sm)

        val obj = object:androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                var intent = Intent(applicationContext,
                    ReciveSearhResults::class.java)
                intent.putExtra(SearchManager.QUERY,query)
                println("text submit")
                intent.action= Intent.ACTION_SEARCH
                startActivity(intent)
                return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {

            return true
        }
    } as SearchView.OnQueryTextListener
        searchMenu.setOnQueryTextListener(obj)
}


     */
    private fun setSearchInfo(
        searchMenu: SearchView,
        sm: SearchManager
    ) {
        searchMenu.setSearchableInfo(
            sm.getSearchableInfo(
                ComponentName(
                    applicationContext,
                    ReciveSearhResults::class.java
                )
            )
        )
    }

    override fun onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }    }

    override fun onSupportNavigateUp(): Boolean {
        println("slected: onSupportNavigationUp")
        return super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onStart() {
        super.onStart()
    }

    override fun getClassName(): String {
        return Explore_Activity::class.qualifiedName!!
    }

    override fun getNavigationView(): NavigationView {
        return mNavigationview
    }

    override fun getDrawerLayout(): DrawerLayout {
        return mDrawerLayout
    }

    override fun getContext(): Context {
    return this
    }

    override fun getToolbar(): Toolbar {
        return toolbar
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun reciveSearchResult() {

    }

    override fun reciveSearchResult(splist:ArrayList<SeriesProperties>) {
        splist.forEach {
            println(it.getString())
        }
    }

    override fun send(resultCode: Int, resultData: Bundle?) {


    }


    /*

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CreateLoader(id)
    }

    fun CreateLoader(id:Int):CursorLoader{
        var selection = SuggetionsDataHelper.TAGS + " LIKE ?"
        var selectionArgs = arrayOf(SeriesProperties.TAGS[id])
        return CursorLoader(this,ContentPropvider_Suggestions.RANDOM_URI,null,selection,selectionArgs,"random()")
    }


    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
    var splist = SuggestionsDataAdapter.getSeriesPropertiesListFromCursor(data!!)
        mAdapter.srlist.add(SeriesRawProperties(SeriesProperties.TAGS[loader.id],splist))
        mAdapter.notifyItemInserted(mAdapter.srlist.size-1)
        if(mAdapter.srlist.size>=11){
            val progressBar = findViewById<ProgressBar>(R.id.activity_explore_progressBar)
            progressBar.post {
                progressBar.visibility=View.GONE
            }
        }
    }


    override fun onLoaderReset(loader: Loader<Cursor>) {
        loader.cancelLoad()
    }

     */


}
