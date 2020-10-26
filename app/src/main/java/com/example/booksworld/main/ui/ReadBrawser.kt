package com.example.booksworld.main.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout

import com.example.booksworld.R
import com.example.booksworld.main.CustomUtils.FileHelper
import java.io.File
import java.lang.Boolean
import java.lang.reflect.InvocationTargetException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReadBrawser.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReadBrawser : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var drawerLayout: DrawerLayout

    lateinit var mWebView: WebView
    var index = 0
    var pages = ArrayList<String>()
    lateinit var mTextView: TextView

    var scrolly = 0
    var fontSize = 4
    var SHARED_PREF_FONT_SIZE="FONT_SIZE"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
            println("on create is called , calling FileToOpenpages")




    }

    fun getContent():String{

        var content  = "<body style=\"background-color:E0E0E0;\" > <font size=\"$fontSize\" style=\"color:616161;\">" +
                "${pages[index]}" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "</font >"+
                "</body> "
        return content
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
      var v=  inflater.inflate(R.layout.fragment_read_brawser, container, false)
        var sharedPref = activity!!.getSharedPreferences(
            PREFENCE_FILE_KEY, Context.MODE_PRIVATE);
        scrolly= sharedPref.getInt(SHARED_PREFERENCES_SCROLL_POSITION,0)

        fontSize = sharedPref.getInt(SHARED_PREF_FONT_SIZE,5)

        index =sharedPref.getInt(SHARED_PREFERENCE_PAGE_NUMBER,0)
        pages = FileHelper.getPages(File(activity!!.filesDir,"Main"))

        var page = pages[index]
        println("patge is "  +page)
        /*
        var content = "<body> " +
                "<p> " +
                "${page.lines}" +
                "</p>" +
                "</body>"
*/

        mWebView = v.findViewById<WebView>(R.id.webview)
        mWebView.loadDataWithBaseURL("file:///android_asset/",getContent(),"text/html", "UTF-8",null)
        mWebView.scrollY=scrolly

        mTextView = v.findViewById(R.id.current_page)
        mTextView.setText("p:${index+1}/${pages.size}")
        allowUses()
        var nextbutton = v.findViewById<Button>(R.id.nextbutton)
     var   prev_button = v.findViewById<Button>(R.id.prev_button)
        nextbutton.setOnClickListener {
            nextButtonClick(v)
        }
        prev_button.setOnClickListener {
            prevButtonOnCLick(v)
        }

        return v

    }

    override fun onPause() {
            println("ReadBrawserFragment paused")
        var sharedPref = activity!!.applicationContext.getSharedPreferences(
            PREFENCE_FILE_KEY, Context.MODE_PRIVATE);
        sharedPref.edit().apply{
            putInt(SHARED_PREFERENCES_SCROLL_POSITION,mWebView.scrollY)
            commit()
        }
        super.onPause()
    }

    override fun onDestroyOptionsMenu() {
        println("ReadBrawserFragment destroyed")
        super.onDestroyOptionsMenu()
    }

    fun nextButtonClick(v: View){
        if(index+1<pages.size){
            index++
            mWebView.loadDataWithBaseURL("file:///android_asset/",getContent(),"text/html", "UTF-8",null)
            mTextView.setText("p:${index+1}/${pages.size}")
            var sharedPref = activity!!.getSharedPreferences(
                PREFENCE_FILE_KEY, Context.MODE_PRIVATE);
            sharedPref.edit().apply{
                putInt(SHARED_PREFERENCE_PAGE_NUMBER,index)
                commit()
            }
        }else{
            Toast.makeText(activity,"last page" , Toast.LENGTH_LONG).show()
        }

    }
    fun prevButtonOnCLick(v:View){
        if(index-1>=0){
            index--
            mWebView.loadDataWithBaseURL("file:///android_asset/",getContent(),"text/html", "UTF-8",null)
            mTextView.setText("p:${index+1}/${pages.size}")
            var sharedPref = activity!!.getSharedPreferences(
                PREFENCE_FILE_KEY, Context.MODE_PRIVATE);
            sharedPref.edit().apply{
                putInt(SHARED_PREFERENCE_PAGE_NUMBER,index)
                commit()
            }
        }else{
            Toast.makeText(activity,"first page" , Toast.LENGTH_LONG).show()
        }
    }


    fun allowUses() {
        val ws = mWebView.settings
        ws.javaScriptEnabled = true
        ws.allowFileAccess = true
        ws.builtInZoomControls = true
        ws.displayZoomControls = false
        ws.loadWithOverviewMode=true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            try {
                val m1 =
                    WebSettings::class.java.getMethod(
                        "setDomStorageEnabled",
                        *arrayOf<Class<*>>(Boolean.TYPE)
                    )
                m1.invoke(ws, Boolean.TRUE)
                val m2 =
                    WebSettings::class.java.getMethod(
                        "setDatabaseEnabled",
                        *arrayOf<Class<*>>(Boolean.TYPE)
                    )
                m2.invoke(ws, Boolean.TRUE)
                val m3 =
                    WebSettings::class.java.getMethod(
                        "setDatabasePath", *arrayOf<Class<*>>(
                            String::class.java
                        )
                    )
                m3.invoke(ws, "/data/data/${activity!!.packageName}/databases/")
                val m4 =
                    WebSettings::class.java.getMethod(
                        "setAppCacheMaxSize",
                        *arrayOf<Class<*>>(java.lang.Long.TYPE)
                    )
                m4.invoke(ws, 1024 * 1024 * 8)
                val m5 =
                    WebSettings::class.java.getMethod(
                        "setAppCachePath", *arrayOf<Class<*>>(
                            String::class.java
                        )
                    )
                m5.invoke(ws, "/data/data/${activity!!.packageName}/cache/")
                val m6 =
                    WebSettings::class.java.getMethod(
                        "setAppCacheEnabled",
                        *arrayOf<Class<*>>(Boolean.TYPE)
                    )
                m6.invoke(ws, Boolean.TRUE)
            } catch (e: NoSuchMethodException) {
            } catch (e: InvocationTargetException) {
            } catch (e: IllegalAccessException) {
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReadBrawser.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReadBrawser().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        val PREFENCE_FILE_KEY="PREFERENCE_FILE_KEY"
        val SHARED_PREFERENCE_PAGE_NUMBER="SHARED_PREFERENCE_PAGE_NUMBER"
        val SHARED_PREFERENCES_SCROLL_POSITION="SHARED_PREFERENCES_PAGE_SCROLL"
    }
}
