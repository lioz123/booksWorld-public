package com.example.booksworld.main.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booksworld.R
import com.example.booksworld.main.CustomUtils.FileHelper
import com.example.booksworld.main.CustomUtils.NavigationDrawerHelper
import com.example.booksworld.main.CustomUtils.NavigationDrawerProperties
import com.example.booksworld.main.CustomUtils.SharedPrefrencesUtils
import com.example.booksworld.main.Firebase.FirebaseAdapter
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.Services.DownloadService
import com.example.booksworld.main.adapters.CategoryPresentaionRecycleViewAdapter
import com.example.booksworld.main.search_suggestiosn_package.SuggestionsDataAdapter
import com.google.android.material.navigation.NavigationView

class ActivityBooksItmesPresentation : AppCompatActivity() ,NavigationDrawerProperties{
    companion object{
        val ACTION_OPEN_DOWNLOADS_FOLDER ="OPEN_DOWNLOADS_FOLDER"
        val ACTION_PRESENT_CATEGORY ="PRESENT_CATEGORY"
        val CATEGORY_PRESENTATION_KEY = "CATEGORY_PRESENTATION_KEY"
        val ACTIVITY_TO_OPEN="ACTIVITY_TO_OPEN"
    }
    lateinit var mSyncButton:Button
    lateinit var mToolbar:Toolbar
    lateinit var mDrawerLayout:DrawerLayout
    lateinit var mNavigationView:NavigationView
    lateinit var mRecycleView:RecyclerView
    lateinit var mProgressBar: ProgressBar
    lateinit var mAdapter :CategoryPresentaionRecycleViewAdapter
    var activityToOpen=""
    lateinit var mFirebaseAdapter: FirebaseAdapter
    var mMenuMannager=ItemMenuMannager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_category_presentation)

        mSyncButton = findViewById(R.id.activityCategoryPresentationSyncButton)
        mProgressBar =findViewById(R.id.activityCategoryProgressBar)
        mFirebaseAdapter= FirebaseAdapter(this)
        initAnimations()
         activityToOpen= intent.getStringExtra(ACTIVITY_TO_OPEN) as String
        var tags = intent.getStringExtra(CATEGORY_PRESENTATION_KEY)
        initRecycleView(activityToOpen)

        mToolbar = findViewById(R.id.toolbar)
        mDrawerLayout = findViewById(R.id.activity_presentation_drawer_layout)

        mNavigationView= findViewById(R.id.navigation_layout)
        mToolbar.title= if(activityToOpen==BookReader::class.qualifiedName) getString(R.string.downloads) else tags
        setSupportActionBar(mToolbar)

            NavigationDrawerHelper.getInstance(this)
//        mNavigationView.menu.getItem(2).setChecked(true)


    }


    fun initRecycleView(activityToOpen:String){
        Thread(Runnable {
            mRecycleView= findViewById(R.id.activity_presentation_recycle_view)
            mAdapter = CategoryPresentaionRecycleViewAdapter(
                getSeriesPropertiesLists(activityToOpen),
                activityToOpen,
                this
            )
            var layoutManager = GridLayoutManager(this,3)
            mRecycleView.post {
                mRecycleView.apply {
                    adapter=mAdapter
                    this.layoutManager=layoutManager
                    hasFixedSize()
                    startLayoutAnimation()
                    if(activityToOpen==BookReader::class.qualifiedName){
                        mSyncButton.visibility = View.VISIBLE
                    }
                }
            }

        }).start()
    }
    fun initAnimations(){
         if (Build.VERSION.SDK_INT >= 21) {
             var transition =    TransitionInflater.from(this).inflateTransition(R.transition.explode)
            window.enterTransition = transition

        }

    }

    fun getSeriesPropertiesLists(activityToOpen:String):ArrayList<SeriesProperties>{
        var splist= ArrayList<SeriesProperties>()
        when(activityToOpen){
            BookReader::class.qualifiedName->{ splist = getSpFromDownloadActivity(splist) }
            ReciveSearhResults::class.qualifiedName ->{ splist = recieveSearchResult(splist) }
        }

        return splist
    }

    private fun getSpFromDownloadActivity(splist: ArrayList<SeriesProperties>): ArrayList<SeriesProperties> {
        var splist1 = splist
        splist1 = FileHelper.getAllDownloadedBoods(this)

        insertBookList()
        splist1.addAll(mFirebaseAdapter.getBookList().filter {!seriesInList(splist1,it.apply {
            it.didNotDownload=true
        })})
        addEmptyFolderText(splist1)
        return splist1

    }

    private fun insertBookList() {
        if (mFirebaseAdapter.isUserFirstLogIn()) {
            mFirebaseAdapter.insertBookList()
        }
    }

    private fun addEmptyFolderText(splist1: ArrayList<SeriesProperties>) {
        if (splist1.size == 0) {
            val text = findViewById<TextView>(R.id.empty_folder)
            text.post {
                text.visibility = View.VISIBLE
            }
        }
    }

    private fun seriesInList(splist: ArrayList<SeriesProperties>, sp:SeriesProperties): Boolean {
        splist.forEach {
            if(it.uid==sp.uid){
                return true
            }
        }
        return false
    }
    private fun recieveSearchResult(splist: ArrayList<SeriesProperties>): ArrayList<SeriesProperties> {
        var splist1 = splist
        val tags = intent.getStringExtra(CATEGORY_PRESENTATION_KEY)
        val da = SuggestionsDataAdapter(this)
        splist1 = da.getRandomBooksByTags(tags, null)
        return splist1
    }


    override fun onDestroy() {
        onPause()
        super.onDestroy()
    }

    fun onDownloadClicked(v:View){
        mFirebaseAdapter.insertBookList()

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(mToolbar.title==getString(R.string.downloads)){
            mToolbar.inflateMenu(R.menu.download_folder_menu)
        }
        return super.onPrepareOptionsMenu(menu)
    }
    fun onSyncClicked(v:View){
        mAdapter.syncData(this,mSyncButton,mProgressBar)
    }

    private fun syncData(sp: SeriesProperties) {
        var intent = Intent(this, DownloadService::class.java).apply {
            action = DownloadService.SYNC_INTENT_ACTION
        }
        intent.putExtra(SeriesProperties.SERIES_PROPERTIES_KEY, sp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }


    override fun onPause() {
        if(intent.getStringExtra(ACTIVITY_TO_OPEN) as String == BookReader::class.qualifiedName){
 //           var da= StarterDataAdapter(this)
   //         var sp = da.getActivityState()
     //       sp.activity=ActivityCategoryPresentation::class.qualifiedName as String
       //     da.update(sp)
            var values = HashMap<String ,Any>()
            values.put(SharedPrefrencesUtils.LAST_ACTIVITY_KEY,ActivityBooksItmesPresentation::class.qualifiedName as Any)
            SharedPrefrencesUtils.update(values)
        }

        super.onPause()
    }
/*
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.explore_selection->{
                startActivity(
                    Intent(this,
                        Explore_Activity::class.java)
                )
            }
            R.id.home_selection ->{
                var intent =Intent(this, BookReader::class.java)
                var sp = SuggestionsDataAdapter(this).getBook(SharedPrefrencesUtils.getUid())
                intent.putExtra(SeriesProperties.SERIES_PROPERTIES_KEY,sp)
                startActivity(intent)
            }
        }
        return true
    }


 */
    override fun getClassName(): String {
        return ActivityBooksItmesPresentation::class.qualifiedName!!
    }

    override fun getNavigationView(): NavigationView {
       return mNavigationView
    }

    override fun getDrawerLayout(): DrawerLayout {
        return mDrawerLayout
    }

    override fun getContext(): Context {
        return applicationContext
    }

    override fun getToolbar(): Toolbar {
        return mToolbar
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun reciveSearchResult() {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        mMenuMannager.itemClick(item)
        return super.onOptionsItemSelected(item)
    }

    inner class ItemMenuMannager(){
        var delteMode = false
        fun itemClick(item:MenuItem){
            when(item.itemId){
                R.id.delete_selected->changeDeleteMode(item)
            }
        }

        fun changeDeleteMode(item: MenuItem) {

            delteMode=!delteMode
            if(delteMode){
                item.setIcon(null)
            }else{
                item.setIcon(R.drawable.ic_baseline_delete_24_white)
            }
            mAdapter.setEditMode(delteMode)
        }

    }
}
