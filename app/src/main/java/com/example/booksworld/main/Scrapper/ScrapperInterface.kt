package com.example.booksworld.main.Scrapper

import org.jsoup.nodes.Element
import org.jsoup.select.Elements

interface ScrapperInterface {
   // fun getBook(sp: SeriesProperties, mbuilder : NotificationCompat.Builder, nm : NotificationManager): ScrapedBookProperies?
    fun getContent(link: Element):String?
    fun getLinks(link: Element):Elements?
    fun getMainDocument():Element?
}