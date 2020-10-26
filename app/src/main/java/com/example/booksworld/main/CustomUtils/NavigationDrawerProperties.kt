package com.example.booksworld.main.CustomUtils

import android.app.Activity
import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

interface  NavigationDrawerProperties {
    fun getClassName():String
    fun getNavigationView():NavigationView
    fun getDrawerLayout():DrawerLayout
    fun getContext():Context
    fun getToolbar():Toolbar
     fun getActivity(): Activity?

    fun reciveSearchResult()
}