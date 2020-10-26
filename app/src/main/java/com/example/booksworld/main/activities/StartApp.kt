package com.example.booksworld.main.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.booksworld.R
import com.example.booksworld.main.DownloadRecivers.DownloadOnBooksReciver
import com.example.booksworld.main.CustomUtils.SearchReciver
import com.example.booksworld.main.CustomUtils.SharedPrefrencesUtils
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.Services.DownloadService
import com.example.booksworld.main.search_suggestiosn_package.SuggestionsDataAdapter

class StartApp : AppCompatActivity() ,SearchReciver{
    companion object{
        val   SHARED_PREFRENSES_DOCUMENN = "SHARED_PREFERNCES_DOCUMENT"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_app)
        println("initialized shared preferences-start")
        LuanchApp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private val WRITE_EXTERNAL_STORAGE_REQUSET_CODE = 30


    fun checkWriteExternalStoragePermmsion(){
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                }else{
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_EXTERNAL_STORAGE_REQUSET_CODE
                    );

                }
        }else{
            LuanchApp()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        LuanchApp()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun LuanchApp() {
        SharedPrefrencesUtils.INITIALIZE_SHARED_PREFRENCES(this)
        println("initialized shared preferences-end")
        var acitivty = SharedPrefrencesUtils.getActivity()
        var sp = SeriesProperties.getEmptyBuild()
        if (acitivty == "") {
            firstLogIn()
            println("table first luanch end")

        } else {
            OpenActivity(sp, acitivty)

        }
    }

    private fun OpenActivity(
        sp: SeriesProperties,
        acitivty: String
    ) {
        var sp1 = sp
        var uid = SharedPrefrencesUtils.getUid()
        sp1 = SuggestionsDataAdapter(this).getBook(uid)
        sp1.activity = acitivty
        //      var tempSp = SuggestionsDataAdapter(this).getBook(sp.uid)
        loadActivity(sp1)
        println("sp uid is:${sp1.uid} ,page is: ${sp1.page} and")
        println("activity to start:${sp1.activity}")
    }

    private fun firstLogIn() {
        var uid = SharedPrefrencesUtils.getUid()
        var title = findViewById<TextView>(R.id.start_app_textView_copying_files)
        title.visibility = View.VISIBLE
        downloadBookLlist()
    }


    fun  loadActivity(sp:SeriesProperties){
       var intent = Intent(this,Class.forName(sp.activity))
       intent.putExtra(SeriesProperties.SERIES_PROPERTIES_KEY,sp)
       intent.putExtra(ActivityBooksItmesPresentation.ACTIVITY_TO_OPEN,BookReader::class.qualifiedName)
       startActivity(intent)

   }

    fun downloadBookLlist(){
        println("insert values was called")
        var downloadReciver = DownloadOnBooksReciver(this,Handler())
        var intent = Intent(this, DownloadService::class.java).apply {
            action = DownloadService.DOWNLOAD_ALL_BOOKS_ACTION
            putExtra(DownloadService.DOWNLOAD_RECIVER,downloadReciver)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }else{
            startService(intent)
        }

    }

    override fun reciveSearchResult(splist: ArrayList<SeriesProperties>) {


    }

    override fun send(resultCode: Int, resultData: Bundle?) {
        var acitivty=Explore_Activity::class.qualifiedName as String
        println("table first luanch start")
        var handler = Handler(Looper.getMainLooper())
        handler.post {
            var sp =SeriesProperties.getEmptyBuild().apply {
                this.activity=acitivty
            }
            loadActivity(sp)
        }
    }

}
