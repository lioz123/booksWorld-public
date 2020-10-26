package com.example.booksworld.main.PropertiesObjects

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableString
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import org.json.JSONObject
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import java.io.ByteArrayOutputStream
import java.lang.NumberFormatException




class SeriesProperties(
    var uid:Int,
    var name: String?, var url:String?,
    var tags:String?, var author:String?,
    var imgSRC:String?,
    var description:String?) :Parcelable{
    var didNotDownload: Boolean = false
    var deleteMode = false
    val pageDomain ="http://indbooks.in/mirror1/?p"
    var textSize = 4
    var scrollX=0
    var scrolly=0
    var activity=""
    var page= 0
    var raw  = 0
    var spannable= SpannableString("show more")
   lateinit var presentationDescription :String
    var showSpannable  =false
    var imageloaded = false
    lateinit var bitmap: Bitmap
    var hasUpdateData=false
    init {
        imgSRC?.let {
            imgSRC = fixUrl(it)
        }
        url?.let {
            url=fixUrl(it)
        }
    }

    fun fixUrl(url:String): String {
        var str  = url.trim()
        str= str!!.replace("m//","m/")
        return str!!.replace("\"","")
    }
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()

    ) {
        presentationDescription = parcel.readString() as String
        showSpannable = parcel.readByte() != 0.toByte()

        activity=parcel.readString() as String
         page = parcel.readInt()
        scrolly= parcel.readInt()
        scrollX=parcel.readInt()

        raw = parcel.readInt()

        if(imageloaded) {
            bitmap = parcel.readParcelable<Bitmap>(Bitmap::class.java.classLoader) as Bitmap
        }
        imageloaded = parcel.readByte() != 0.toByte()

        println(getString())

    }

    init {
        handleDescriptionDisplay()
    }

    private fun handleDescriptionDisplay() {
        if(description==null){
            presentationDescription=""
            return
        }
        presentationDescription = description as String
        if (presentationDescription.length > 500) {
            presentationDescription = presentationDescription.substring(0, 500) + "... "
            showSpannable = true
        }
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        writeValuesToParcel(parcel)
        writeBitmap(parcel, flags)
    }

    private fun writeBitmap(parcel: Parcel, flags: Int) {
        if (this::bitmap.isInitialized && getImageSize(bitmap) < MAX_IMAGE_SIZE) {
            println("image size is ${getImageSize(bitmap)}")
            parcel.writeParcelable(bitmap, flags)
            parcel.writeByte(1)
        } else {
            parcel.writeByte(0)
        }
    }

    private fun getImageSize(bitmap: Bitmap): Int {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
        return stream.toByteArray().size
    }

    private fun writeValuesToParcel(parcel: Parcel) {
        parcel.writeInt(uid)
        parcel.writeString(name)
        parcel.writeString(url)
        parcel.writeString(tags)
        parcel.writeString(author)
        parcel.writeString(imgSRC)
        parcel.writeString(description)
        parcel.writeString(presentationDescription)
        parcel.writeByte(if (showSpannable) 1 else 0)
        parcel.writeString(activity)
        parcel.writeInt(page)
        parcel.writeInt(scrolly)
        parcel.writeInt(scrollX)
        parcel.writeInt(raw)
    }


    fun getString():String{
        return "Uid:$uid name:$name url:${url} tags:${tags} author:${author} imgSRC:${imgSRC} decription:$description showSpannable:${showSpannable} imageLoaded:${imageloaded} activity:${activity} page:${page} scrollY:${scrolly} scrollX:${scrollX} raw:${raw}"
    }
    override fun describeContents(): Int {
        return 0
    }

    fun getUrlAsElement(): Element? {
        val el = Element(Tag.valueOf("a"),url)
        el.attr("href",url!!.split(pageDomain)[0])
        return el
    }

    fun toMap(): HashMap<String, Any> {
        return HashMap<String,Any>().apply {
            put(UID,uid)
            put(NAME,name!!)
            put(URL,url!!)
            put(TAGS,tags!!)
            put(AUTHOR,author!!)
            put(IMG_SRC,imgSRC!!)
            put(DESCRIPTION, description!!)
            put(URL,url!!)
            put(PAGE, page)
            put(SCROLL_Y,scrolly)

        }
    }
    fun update(snapshot: DocumentSnapshot?) {
        println("tryig to take update for uid:${uid}")
        if (!this.hasUpdateData) {
            snapshot?.let { //
                    this.hasUpdateData = true
                    println("name:${snapshot.get(NAME)}")
                   snapshot.getString(NAME)?.let {
                       name=  it
                       print("name is$name")
                   }
                     snapshot.getString(URL)?.let {
                         url=  it
                         print("url is$url")
                     }

                      snapshot.getString(TAGS)?.let {
                          tags=  it
                          print("tags is$tags")

                      }
                    snapshot.getString(AUTHOR)?.let {
                        author=  it
                        print("author is$author")

                    }
                      snapshot.getString(DESCRIPTION)?.let {
                          description=  it
                          print("description is$description")

                      }
                snapshot.getString(IMG_SRC)?.let {

                    imgSRC=  it
                    print("imgSRC is$imgSRC")

                }
                try {
                        println("page is ${snapshot.get(PAGE)}")
                        snapshot.getLong(PAGE)?.let {

                            page = it.toInt()
                            print("page is$page")

                        }

                    }catch (e:NumberFormatException){
                        e.printStackTrace()
                    }
                    try{

                    }catch (e:NumberFormatException){
                        snapshot.getLong(SCROLL_Y)?.let {

                            print("scrolly is$scrolly")
                            scrolly = it.toInt()
                        }
                        e.printStackTrace()
                    }



                    handleDescriptionDisplay()


            }
        }
    }

    companion object CREATOR : Parcelable.Creator<SeriesProperties> {
        val TAG="seriesProperties.class"
        val SERIES_PROPERTIES_KEY="SERIES_PROPERTIES_KEY"
        val PAGE = "PAGE"
        val SCROLL_Y="scrolly"
        val SPLITTER ="SPLITER_BOOKS"
        val ADVENTURE= "Adventure "
        val CHRISTIAN="Christian"
        val FANTASY= "Fantasy"
        val GENERAL = "General"
        val HISTORICAL="Historical"
        val HORROR = "Horror"
        val WESTERN="Western"
        val HUMOROUS="Humorous"
        val MYSTERY="Mystery"
        val ROMANCE="Romance"
        val  SCIENCE_FICTION="Science Fiction"
        val THRILLER="Thriller"
        val YOUNG_ADULT="Young Adult"
        val UID="uid"
        val NAME="name"
        val URL="url"
        val TAGS= "tags"
        val AUTHOR ="author"
        val DESCRIPTION ="description"
        val IMG_SRC="imgSrc"
        private val MAX_IMAGE_SIZE = 60000
        val TAGS_LIST = arrayListOf<String>(ADVENTURE, CHRISTIAN, FANTASY, GENERAL, HISTORICAL, HORROR,
            WESTERN, HUMOROUS, MYSTERY,ROMANCE, SCIENCE_FICTION, YOUNG_ADULT)
        fun Builder(str:String):SeriesProperties{
            var prs = str.split(SPLITTER)
            return SeriesProperties(-1,prs[0],prs[1],prs[2],prs[3],prs[4],prs[5])
        }

        fun BuildSmallSeriesPropertiesFromLine(line:String): SeriesProperties {
            var arr = line.split(SPLITTER)
            var name = arr[0]
            var imgSrc = arr[1]
            return SeriesProperties(-1,name,"","","",imgSrc,"")

        }
        fun BuildSeriesPropertiesFromJSON(json:JSONObject): SeriesProperties {
            return SeriesProperties(-1,json.getString(NAME),json.getString(URL),json.getString(TAGS),json.getString(
                AUTHOR),json.getString(IMG_SRC),json.getString(DESCRIPTION))
        }

        fun BuildFromSnapshot(snapshot: DocumentSnapshot):SeriesProperties{
            var uid = snapshot.getLong(UID)?.toInt()
            var page = snapshot.getLong(PAGE)?.toInt()
            if(uid==null){
                uid =-1
            }
            if(page==null){
                page=0
            }
            return SeriesProperties(uid,snapshot.getString(NAME),snapshot.getString(URL),snapshot.getString(TAGS),snapshot.getString(
                AUTHOR),snapshot.getString(IMG_SRC),snapshot.getString(DESCRIPTION)).apply {
                this.page=page
                println("build from snapshot ${getString()}")
            }
        }
        fun BuildSmallSeriesProperties(uid:Int,name:String,imgSrc: String?): SeriesProperties {
            return SeriesProperties(uid,name,"","","",imgSrc,"")
        }

        fun getEmptyBuild():SeriesProperties{
            return SeriesProperties(-1,"","","","","","")
        }


        override fun createFromParcel(parcel: Parcel): SeriesProperties {
            return SeriesProperties(parcel)
        }

        override fun newArray(size: Int): Array<SeriesProperties?> {
            return arrayOfNulls(size)
        }

        fun BuildFromSeriesProperties(sp: SeriesProperties): SeriesProperties {
        var tempSp = SeriesProperties(sp.uid,sp.name,sp.url,sp.tags,sp.author,sp.imgSRC,sp.description)

            return tempSp
        }
    }

    fun print(str: String){
        Log.d(TAG,str)
    }
}