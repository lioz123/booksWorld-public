package com.example.booksworld.main.Scrapper

class ScrapperMannger {
    /*
    lateinit var contentList :Array<String?>
    fun getGeneralBook(c: Context, scrapper:ScrapperInterface){
        var numberOfThreads = PreferenceManager.getDefaultSharedPreferences(c).getInt(c.getString(
                    R.string.number_of_threads),1)
        contentList = arrayOfNulls(numberOfThreads)
        val doc = scrapper.getMainDocument()
        if(doc==null){
            return
        }
        val links = getLinksChunk(scrapper, doc, numberOfThreads)
        val es = Executors.newFixedThreadPool(links.size)
        links.forEachIndexed {index,links->
            es.execute(DownloadBookTask(this,index,scrapper,links))
        }
        es.shutdown()
        try{
            es.awaitTermination(10000, TimeUnit.MICROSECONDS)
        }catch (e:InterruptedException){

        }
    }
    @Synchronized fun updateList(content:String, index:Int){
        contentList[index] =content
    }
    private fun getLinksChunk(
        scrapper: ScrapperInterface,
        doc: Element,
        numberOfThreads: Int
    ): List<List<Element>> {
        var elementsLinks = scrapper.getLinks(doc)
        var links = elementsLinks.chunked(elementsLinks.size / numberOfThreads)
        return links
    }

     */
}