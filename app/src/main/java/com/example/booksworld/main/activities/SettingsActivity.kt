package com.example.booksworld.main.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceFragmentCompat
import com.example.booksworld.R
import com.example.booksworld.main.CustomUtils.NavigationDrawerHelper
import com.example.booksworld.main.CustomUtils.NavigationDrawerProperties
import com.google.android.material.navigation.NavigationView

class SettingsActivity : AppCompatActivity(), NavigationDrawerProperties {
    lateinit var mToolbar: Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings,
                SettingsFragment()
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mToolbar = findViewById(R.id.toolbar)
        mToolbar.setTitle(R.string.settings_selection)
        setSupportActionBar(mToolbar)
        NavigationDrawerHelper.getInstance(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
       setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun getClassName(): String {
        return SettingsActivity::class.qualifiedName!!
    }

    override fun getNavigationView(): NavigationView {
        return findViewById(R.id.navigation_layout)
    }

    override fun getDrawerLayout(): DrawerLayout {
        return findViewById(R.id.drawer_layout)
    }

    override fun getContext(): Context {
        return this
    }

    override fun getToolbar(): Toolbar {
        return mToolbar
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun reciveSearchResult() {
        TODO("Not yet implemented")
    }
}