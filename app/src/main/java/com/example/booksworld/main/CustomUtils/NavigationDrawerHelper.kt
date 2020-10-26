package com.example.booksworld.main.CustomUtils

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.booksworld.R
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.activities.*
import com.example.booksworld.main.activities.LogInActivity.Companion.LOG_OUT_ACTION
import com.example.booksworld.main.search_suggestiosn_package.SuggestionsDataAdapter
import com.google.android.material.navigation.NavigationView

class NavigationDrawerHelper(var navigationProperties:NavigationDrawerProperties): NavigationView.OnNavigationItemSelectedListener{
    var mSelectedId =0
    var selectedHashMap = HashMap<String,Any>()
    var intentToStart = Intent(navigationProperties.getContext(),Explore_Activity::class.java)
    var isStartingIntent = false
    var mSp :SeriesProperties
    init {
        mSp = SuggestionsDataAdapter(navigationProperties.getContext()).getBook(SharedPrefrencesUtils.getUid())

        selectedHashMap.put(SeriesProperties.SERIES_PROPERTIES_KEY,mSp)
        selectedHashMap.put( ActivityBooksItmesPresentation.ACTIVITY_TO_OPEN,BookReader::class.qualifiedName!!)

        navigationProperties.getNavigationView().menu.getItem(getPositionFromClassName(navigationProperties.getClassName())).isChecked = true
        var toogle = object :ActionBarDrawerToggle(navigationProperties.getActivity(),navigationProperties.getDrawerLayout(),navigationProperties.getToolbar(),
        R.string.nav_app_bar_open_drawer_description,
        R.string.close_drawer

    ){
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if(isStartingIntent){
                navigationProperties.getContext().startActivity(intentToStart)
                }

            }

        }

        navigationProperties.getDrawerLayout().addDrawerListener(toogle)
        navigationProperties.getNavigationView().setNavigationItemSelectedListener(this)
        navigationProperties.getNavigationView().bringToFront()
        toogle.syncState()
    }



    companion object{

        fun getInstance(drawerProperties: NavigationDrawerProperties):NavigationDrawerHelper{
            return NavigationDrawerHelper(drawerProperties)
        }
        fun getPositionFromClassName(className:String):Int{
            when(className){
                ActivityBooksItmesPresentation::class.qualifiedName!! -> return 2
                Explore_Activity::class.qualifiedName as String   ->return   1
                BookReader::class.qualifiedName as String ->return  0
                SettingsActivity::class.qualifiedName as String->return 3
                LogInActivity::class.qualifiedName as String -> return 4
            }
            return 0
        }
        fun getClassNameFromItemId(itemId:Int):String{
            when(itemId){
                R.id.downloads_selection-> return ActivityBooksItmesPresentation::class.qualifiedName!!
                R.id.explore_selection ->return Explore_Activity::class.qualifiedName as String
                R.id.home_selection->return BookReader::class.qualifiedName as String
                R.id.settings_selection->return  SettingsActivity::class.qualifiedName as String
                R.id.log_out_selection ->return  LogInActivity::class.qualifiedName as String
            }
            return ""
        }



        fun startActivityFromSelectedItem(context: Context,itemId: Int,className:String,map:HashMap<String,*> = HashMap<String,String>()): Intent? {
            var activityToOpen = getClassNameFromItemId(itemId)
            if(activityToOpen!=className){
                var intent = Intent(context, Class.forName(activityToOpen))
                map.keys.forEach {
                    var obj = map.get(it)
                    when(obj){
                        is Parcelable -> intent.putExtra(it,obj)
                        is Boolean -> intent.putExtra(it,obj)
                        is Int -> intent.putExtra(it,obj)
                        is Float ->intent.putExtra(it,obj)
                        is String ->intent.putExtra(it,obj)
                        is Double -> intent.putExtra(it,obj)
                    }

                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                return intent
            }
            return null
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
    //    navigationProperties.getDrawerLayout().closeDrawer(GravityCompat.START)

            mSelectedId=item.itemId
            var tempIntent =startActivityFromSelectedItem(navigationProperties.getContext(),mSelectedId,navigationProperties.getClassName(),selectedHashMap)

            if(tempIntent!=null){
                intentToStart=tempIntent
                isStartingIntent = true
                if(getClassNameFromItemId(mSelectedId)==LogInActivity::class.qualifiedName as String){
                   intentToStart.action= LOG_OUT_ACTION
                }
                intentToStart.action = LOG_OUT_ACTION
                navigationProperties.getContext().startActivity(intentToStart)

            }else{

            }


        return true
    }
}