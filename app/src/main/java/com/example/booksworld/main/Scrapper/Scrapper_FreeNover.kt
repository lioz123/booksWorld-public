package com.example.booksworld.main.Scrapper

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException

class Scrapper_FreeNover(sp: SeriesProperties,
                         mbuilder: NotificationCompat.Builder, nm: NotificationManager
) : ScrapperAbstacted(sp, mbuilder, nm) {
    companion object{
        val DOMAIN = "http://onlinereadfreenovel.com"

    }

    override fun getLinks(link: Element): Elements? {
        val pagesDiv= link.getElementsByClass("pages")
        if(pagesDiv.isNullOrEmpty()){
            return null
        }
       val links= pagesDiv[0].getElementsByTag("a")
       val mainLink= sp.getUrlAsElement()

        links.add(0,mainLink)
        links.forEach {
            println("link is:"+link.html())
        }

        return links

    }

    override fun getContent(link: Element): String? {
        var href = link.attr("href")
        println("page href:${href}")
        try {
            return getPageContent(href)
        } catch (e: IOException) {
            return null
        }
    }

    fun getPageContent(href:String) :String{
        var url = if(href.indexOf(DOMAIN)!=-1) href else DOMAIN+href

        var doc = Jsoup.connect(url).get()
        var element = doc.getElementById("textToRead")
        if(element==null) throw IOException("There is not such object")
        var str = element.html().toString()
        return str
    }
    override fun getMainDocument(): Element? {
        try{
            return Jsoup.connect(sp.url).get()

        }catch (e:IOException){
            return null
        }

    }
}