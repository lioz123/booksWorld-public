package com.example.booksworld.main.Scrapper

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException

class Scrapper_NovelFree(
    sp: SeriesProperties,
    mbuilder: NotificationCompat.Builder,
    nm: NotificationManager
) : ScrapperAbstacted(sp, mbuilder, nm) {
   companion object{
     val  DOMAIN="https://novelfree.net"

   }

    override fun getContent(link: Element): String? {
        try {
            var doc = Jsoup.connect(link.attr("href")).timeout(180 * 1000).get()
            val div = doc.getElementById("des_novel") ?: return null
           var divs= div.getElementsByTag("div")

           for(i in 0 until divs.size){
               divs[i].remove()
           }
            val ps= div.getElementsByTag("p")
            var content =""
            ps.forEach {
                content+=it.html() +"</br></br>"
            }
            println(content)
            return content
        }catch (e:IOException){
            return null
        }

    }

    override fun getLinks(link: Element): Elements? {
        var bodys = link.getElementsByTag("tbody")
        if(bodys.size==0){
            return null
        }
        var links = bodys.first().getElementsByTag("a")
        if(links.size==0){
            return null
        }
        links.forEach {
            var href = it.attr("href")
            var smeltHref = DOMAIN+href
            it.attr("href",smeltHref)

        }
        return links
    }

}