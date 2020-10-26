package com.example.booksworld.main.Scrapper

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.lang.Exception
import java.net.MalformedURLException

class Scrapper_INDBOOKS(sp:SeriesProperties,
                        mbuilder: NotificationCompat.Builder, nm: NotificationManager
):ScrapperAbstacted(sp, mbuilder, nm){

    override fun getContent(link: Element): String? {
        try{
            val content = getPageContent(link)
            return content
        }catch (e:MalformedURLException){
            return ""
        }catch (e:IOException){
            return try {
                e.printStackTrace()
                return getPageContent(link)

            }catch (e:IOException){
                e.printStackTrace()
                println("there is an ioe exeption")
                null
            }

        }catch (e:Exception){
            return ""
        }

    }

    private fun getPageContent(link: Element): String {
        var content = ""
        var doc = Jsoup.connect(link.attr("href")).timeout(180 * 1000).get()
        println("succeed to connect to doc:${link.attr("href")}")
        var plist = doc.getElementsByAttributeValue("style", "text-align: justify;")
        if (plist == null) {
            println("null object")
            throw IOException()
        }
        plist.forEach {
            content += it.html() + "</br></br>"
        }
        println("content is:$content")
        return content
    }

    override fun getLinks(link: Element): Elements? {
      return  link.getElementsByTag("a")
    }

}