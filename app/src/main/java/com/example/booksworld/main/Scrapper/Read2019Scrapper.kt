package com.example.booksworld.main.Scrapper

import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException

class Read2019Scrapper(sp: SeriesProperties,
                       mbuilder: NotificationCompat.Builder, nm: NotificationManager

) : ScrapperAbstacted(sp, mbuilder, nm) {
    init {
        domain = "https://www.reads2019.com"
    }

    override fun getLinks(link: Element): Elements? {
      val selector = link.getElementsByAttributeValue("name","paging_drop_down_page")
        var options:Elements?
        options = null
        println("selector size:${selector.size}")
        if(selector.size>0) {
           options= selector[0].getElementsByTag("option").apply {
               println("size:${size}")
               forEach {
                   val url = Inject(it.attr("value"))
                   it.attr("href", url)
               }
           }
        }
        return options

    }

    override fun getContent(link: Element): String? {
            val url = link.attr("href")
        try{
            val doc = Jsoup.connect(url).get()

            val contents =doc.getElementsByTag("p")
            var str = ""
            println("classs content size:${contents.size}")
                contents.forEach {
                    str+=it.text() + "</br></br>"
                }
            return str

        }catch (e:IOException){
            Log.e("BUG","Unable to connect ot url")
            e.printStackTrace()
        }

        return null
    }

    override fun getMainDocument(): Element? {
        try{
            var doc=  Jsoup.connect(sp.url).get()
            println(doc.html())
            return doc
        }catch (e:IOException){
            e.printStackTrace()
        }
        return null
        }


}
