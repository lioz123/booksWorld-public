
package com.example.booksworld.main.Services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.booksworld.R
import com.example.booksworld.main.DownloadRecivers.DownloadOnBooksReciver
import com.example.booksworld.main.CustomUtils.FileHelper
import com.example.booksworld.main.DownloadRecivers.DownloadReciver
import com.example.booksworld.main.Firebase.FirebaseAdapter
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.Scrapper.ScrapedBookProperies
import com.example.booksworld.main.Scrapper.ScrapperAbstacted
import com.example.booksworld.main.activities.Explore_Activity
import com.example.booksworld.main.search_suggestiosn_package.SuggestionsDataAdapter
import com.example.booksworld.main.server_mannager.ServerHelper
import java.io.*
import java.net.URL

class DownloadService() : IntentService(DownloadService::class.simpleName) {
    companion object{
        fun startDownloadSeries(c: Context, sp: SeriesProperties,downloadReciver: DownloadReciver?=null) {
            println("downllaod series:${sp.getString()}")
             SuggestionsDataAdapter(c).update(sp)
            var intent = Intent(c, DownloadService::class.java).apply {
                action = DownloadService.DOWNLOAD_BOOK_ACTION
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
           var tempSp = SeriesProperties.BuildFromSeriesProperties(sp)
            intent.putExtra(SeriesProperties.SERIES_PROPERTIES_KEY, tempSp)
            if(downloadReciver==null){
                println("reciver at startDownloadSeries is null")
            }else{
                println("reciver at startDownloadSeries is not null")

            }
            intent.putExtra(DownloadReciver.TAG,downloadReciver)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                c.startForegroundService(intent)
            } else {
                c.startService(intent)
            }
        }


        val DOWNLOAD_RECIVER="DOWNLOAD_RECIVER"
        var STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION"
        var CHANNEL_ID ="DOWNLOAD_CHANNEL"
        val MAIN_FOLDER = "Books"
        val BOOK_KEY= "book.txt"
        val IMAGE_KEY="image.png"
        val IMAGES_DIR="IMAGES"
        val NOTIFICATION_ID=12
        var RUNNING = false
        var PAGES_FOLDER="PAGES_FOLDER"
        var FAILED_DOWNLOADING_ID=13
        val DOWNLOAD_BOOK_ACTION="DOWNLOAD_BOOK_ACTION"
        val DOWNLOAD_ALL_BOOKS_ACTION="DOWNLOAD_ALL_BOOKS_ACTION"
        val SEARCH_BOOKS_ACTION="SEARCH_BOOKS_ACTION"
        val SYNC_INTENT_ACTION = "SYNCE_BOOK_ACTION"
        val SEARCH_RESULT="SEARCH_RESULT"
        lateinit    var   mNotifyManager:NotificationManager

        @Synchronized fun setRunning(b:Boolean){
            RUNNING=b
        }
    }



    lateinit var mBuilder :NotificationCompat.Builder
    override fun onDestroy() {
        RUNNING=false
        super.onDestroy()
    }



    override fun onHandleIntent(intent: Intent?) {
        println("Action")
        when(intent?.action){

            DOWNLOAD_BOOK_ACTION-> DownloadBook(intent)
            DOWNLOAD_ALL_BOOKS_ACTION->DownloadAllBooks(intent)
            SEARCH_BOOKS_ACTION-> SearchBookAction(intent)
            SYNC_INTENT_ACTION -> handleSynceIntent(intent)
        }

    }
    private fun SearchBookAction(intent:Intent){

       var jsonStr= ServerHelper.SearchBooks(intent.getStringExtra(SEARCH_RESULT)!!,intent.getIntExtra(ServerHelper.SKIP,0),intent.getIntExtra(ServerHelper.LIMIT,10))
        var downloadService = intent.getParcelableExtra<ResultReceiver>(DOWNLOAD_RECIVER)!!
        var bundle = Bundle()
        bundle.putString(DownloadOnBooksReciver.JSON_ARR,jsonStr)
        downloadService.send(DownloadOnBooksReciver.BOOK_SUCCEES_RESULT,bundle)
    }
    private fun DownloadAllBooks(intent:Intent){
        println("Download all books")
        CreateNotification()
        var downloadReciver = intent.getParcelableExtra<ResultReceiver>(DOWNLOAD_RECIVER)!!
        ServerHelper.DownloadBooksList(applicationContext!!,downloadReciver)
    }

    private fun DownloadBook(intent: Intent?) {

        println("Action${intent?.action}")
        RUNNING = true
        val sp = getSeriesProperties(intent)!!
        val reciver = intent?.getParcelableExtra(DownloadReciver.TAG) as ResultReceiver?
        val resultBundle: Bundle = getResultBundle(sp)
        CreateNotification(sp)
        downloadImage(sp)

        val scrapper = ScrapperAbstacted.Builder(sp, mBuilder, mNotifyManager)
        if(scrapper!=null){
            val scrapedBookProperies = scrapper.getGeneralBook(this)
            if (scrapedBookProperies == null) {
                notifyDownloadFailed(sp)
                reciver?.send(DownloadReciver.FAILED_CODE,resultBundle)

                return
            }
            val result = saveBook(sp, scrapedBookProperies)
            if (result) {
                handleImage(sp)
            }
        }

        val adapter= FirebaseAdapter(this)
        adapter.removeBookFromUnDownloadedList(sp)
        if(reciver==null){
            println("reciveri is null")
        }
        reciver?.send(DownloadReciver.SUCCESS_CODE,resultBundle)
        println("book downloaded succesfully")
    }

    private fun getResultBundle(sp: SeriesProperties): Bundle {
        val resultBundle: Bundle = Bundle()
        resultBundle.putInt(SeriesProperties.UID, sp.uid)
        resultBundle.putString(SeriesProperties.NAME, sp.name)
        return resultBundle
    }

    private fun getSeriesProperties(intent: Intent?): SeriesProperties? {
        var sp =
            intent!!.getParcelableExtra<SeriesProperties>(SeriesProperties.SERIES_PROPERTIES_KEY)

        return sp
    }

    fun CreateNotification(sp:SeriesProperties){
        println("creating notification for:${sp.getString()}")
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            var serviceChannel = NotificationChannel(CHANNEL_ID,"auto gater foreground service location",
                NotificationManager.IMPORTANCE_HIGH)
            var notificationManager= getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }
        var intent = Intent(this, Explore_Activity::class.java)
        var pendingIntent = PendingIntent.getActivity(this,0, intent,0)
        var stopIntent = Intent(this,MainBraodCastReciver::class.java)
        stopIntent.action= STOP_SERVICE_ACTION

        var textContent = "Downloading: ${sp.name}"
        var pendingStop = PendingIntent.getBroadcast(this,30,stopIntent,0)
        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)

        var notification =mBuilder
            .setContentTitle(textContent)
            .setSmallIcon(R.drawable.splash_screen_luncher)
            //   .addAction(com.example.autogater.R.drawable.dismiss_test,"Stop")
            .addAction(R.drawable.splash_screen_luncher,getString(R.string.stop),pendingStop)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent).build()
        //   notificationManager.notify(0,notification)
        mNotifyManager =  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //mNotifyManager.notify(NOTIFICATION_ID,notification)
        startForeground(NOTIFICATION_ID,notification)

    }
    private fun createSynceNotification(sp:SeriesProperties){
        createNotificationChannel()


        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)

        var notification =mBuilder
            .setContentTitle("sync:"+sp.name)
            .setSmallIcon(R.drawable.splash_screen_luncher)
            //   .addAction(com.example.autogater.R.drawable.dismiss_test,"Stop")
            .setOngoing(true)
            .setOnlyAlertOnce(true).build()
        //   notificationManager.notify(0,notification)
        mNotifyManager =  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //mNotifyManager.notify(NOTIFICATION_ID,notification)
        startForeground(NOTIFICATION_ID,notification)
    }

    private fun handleSynceIntent(intent: Intent?){

        var sp = getSeriesProperties(intent)
        sp?.let { sp->
            createSynceNotification(sp)
            val da = SuggestionsDataAdapter(this)
            da.update(sp)
            val firebaseAdapter = FirebaseAdapter(this)
            firebaseAdapter.addUserBook(sp)
        }

    }

    fun CreateNotification(){
        createNotificationChannel()
        var intent = Intent(this, Explore_Activity::class.java)
        var pendingIntent = PendingIntent.getActivity(this,0, intent,0)
        var stopIntent = Intent(this,MainBraodCastReciver::class.java)
        stopIntent.action= STOP_SERVICE_ACTION

        var textContent = getString(R.string.downloading)
        var pendingStop = PendingIntent.getBroadcast(this,30,stopIntent,0)
        mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        var notification =mBuilder
            .setContentTitle(textContent)
            .setSmallIcon(R.drawable.splash_screen_luncher)
            //   .addAction(com.example.autogater.R.drawable.dismiss_test,"Stop")
            .addAction(R.drawable.splash_screen_luncher,getString(R.string.stop),pendingStop)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent).build()
        //   notificationManager.notify(0,notification)
        mNotifyManager =  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //mNotifyManager.notify(NOTIFICATION_ID,notification)
        startForeground(NOTIFICATION_ID,notification)

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var serviceChannel = NotificationChannel(
                CHANNEL_ID, "bookswolrd synce service",
                NotificationManager.IMPORTANCE_HIGH
            )
            var notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun notifyDownloadFailed(sp:SeriesProperties){
        mNotifyManager.cancel(NOTIFICATION_ID)
        var notification = NotificationCompat.Builder(this, CHANNEL_ID).
        setContentTitle(getString(R.string.failed_downloading)+sp.name)
            .setSmallIcon(R.drawable.book_content_image_background)
            .  build()
        mNotifyManager.notify(FAILED_DOWNLOADING_ID,notification)
    }

    fun saveBook(sp:SeriesProperties,scrapedBookProperies: ScrapedBookProperies):Boolean{
       // var maindir = File(applicationContext.filesDir, MAIN_FOLDER)
        var maindir = FileHelper.getMainFolder(applicationContext)
        if(!maindir.exists()){
            maindir.mkdir()
        }

        var bookdir = File(maindir,sp.name+SeriesProperties.SPLITTER+sp.uid)
        if(!bookdir.exists()){
            bookdir.mkdir()
            FileHelper.write(bookdir,scrapedBookProperies.content)
            var imgDir = File(bookdir, IMAGES_DIR)
            imgDir.mkdir()
            FileHelper.saveImages(imgDir,scrapedBookProperies.bitmaps)
            return true

        }else{
            println("book is already exist")
            Toast.makeText(applicationContext,"book exists",Toast.LENGTH_LONG).show()
            return false
        }

    }

    fun handleImage(sp:SeriesProperties){

        var maindir = FileHelper.getMainFolder(applicationContext)
        var bookdir = File(maindir,sp.name+SeriesProperties.SPLITTER+sp.uid)
        var image  = File(bookdir,"image.png")
        var bitmap = sp.bitmap
        println("is series loaded: " +sp.imageloaded)
        if(!sp.imageloaded){
            Thread(Runnable {
                var bitmap= downloadImage(sp)
                if(bitmap!=null){
                    FileHelper.saveImage(bookdir,bitmap,"image")
                }
            }).start()
        }else{
            FileHelper.saveImage(bookdir,bitmap, IMAGE_KEY)
        }


    }
    fun downloadImage(sp:SeriesProperties):Bitmap?{
        try {
            var url = URL(sp.imgSRC)
            var bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            sp.bitmap=bitmap
            sp.imageloaded=true
            println("download bitmap")
            return bitmap

        }catch (e:Exception){
            println("failed to download bitmap")

            e.printStackTrace()
        }
        return null
    }



}