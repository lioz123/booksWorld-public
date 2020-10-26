package com.example.booksworld.main.Scrapper

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.example.booksworld.R
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.Services.DownloadService
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract  class ScrapperAbstacted(var sp: SeriesProperties,var mbuilder : NotificationCompat.Builder,var nm : NotificationManager) :ScrapperInterface{
        companion object{
            var FREE_NOVEL="onlinereadfreenovel"
            var INDBOOKS ="indbooks"
            var READ2019="https://www.reads2019.com"
            val NOVELFREE ="https://novelfree.net"
        fun Builder(sp:SeriesProperties, mbuilder : NotificationCompat.Builder, nm : NotificationManager): ScrapperAbstacted? {
            if(sp.url==null){
                return null
            }
            if(sp.url!!.indexOf(INDBOOKS)!=-1){
                return Scrapper_INDBOOKS(sp,mbuilder,nm)
            }else if(sp.url!!.indexOf(FREE_NOVEL)!=-1){
                return Scrapper_FreeNover(sp,mbuilder,nm)
            }else if(sp.url!!.indexOf(READ2019)!=-1){
                return Read2019Scrapper(sp,mbuilder,nm)
            }else if(sp.url!!.indexOf(NOVELFREE)!=-1){

                return Scrapper_NovelFree(sp,mbuilder,nm)
            }
            return null
        }

    }
    var domain=""
        var mProgress=0
    var mLinksSize = 0
    lateinit var contentList :Array<String?>

    fun getGeneralBook(c: Context):ScrapedBookProperies?{
        try{
            var numberOfThreads = PreferenceManager.getDefaultSharedPreferences(c).getInt(c.getString(
                R.string.number_of_threads),6)
            println("number of threads:${numberOfThreads}")
            val doc = getMainDocument() ?: return null
            val elementsLinks = setLinksSize( doc)
            if(elementsLinks.size==0){
                return null
            }
            println("element links size is:${elementsLinks.size} number of threads is:${numberOfThreads}")
            val links = getLinksChunk( elementsLinks, numberOfThreads)
            val es = Executors.newFixedThreadPool(links.size)
            contentList = arrayOfNulls(links.size)

            links.forEachIndexed {index,links->
                es.execute(DownloadBookTask(this,index,links))
            }
            es.shutdown()
            var finished = es.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);

            if(DownloadService.RUNNING){
                return getScrapedProperties()
            }else{
                return null
            }

        }catch (e:IOException){
            e.printStackTrace()
            return null
        }


    }

    private fun getScrapedProperties(): ScrapedBookProperies {
        var content = ""
        val bitmaps = HashMap<String, Bitmap>()
        contentList.forEach {
            content += it
        }
        return ScrapedBookProperies(content, bitmaps)
    }

    private fun setLinksSize(
        doc: Element
    ): Elements {
        val elementsLinks = getLinks(doc)
        if(elementsLinks==null){
            throw IOException("Function setLinksSize: elements links is null")
        }
        mLinksSize = elementsLinks.size
        return elementsLinks
    }

    @Synchronized fun notifyProgression(){
        mProgress++
        mbuilder.setProgress(mLinksSize,mProgress,false)
        nm.notify(DownloadService.NOTIFICATION_ID,mbuilder.build())
    }
    @Synchronized fun updateList(content:String, index:Int){
        contentList[index] =content
    }
    private fun getLinksChunk(
        elementsLinks: Elements,
        numberOfThreads: Int
    ): List<List<Element>> {
        val sum = if(elementsLinks.size>numberOfThreads) elementsLinks.size / numberOfThreads else numberOfThreads
            val links = elementsLinks.chunked(sum)
            return links
    }


    fun Inject(url:String):String{

        return domain+url
    }

    inner class DownloadBookTask(
        var scrapperMannager: ScrapperAbstacted,
        var index:Int,
        var links:List<Element> ):Runnable {

        override fun run() {
            var content =""
            links.forEach {

                if(!DownloadService.RUNNING){
                    return
                }
                var str = getContent(it)
                println("content is:${str}")
                if(str==null){
                    DownloadService.setRunning(false)
                }
                content+=str
                notifyProgression()
            }

            scrapperMannager.updateList(content,index)
        }
    }


    override fun getMainDocument(): Element? {
        try{
            return Jsoup.connect(sp.url).get()
        }catch (e:IOException){
            return null
        }
    }
}