package com.example.booksworld.main.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.example.booksworld.R
import com.example.booksworld.main.CustomUtils.FileHelper
import com.example.booksworld.main.CustomUtils.NavigationDrawerHelper
import com.example.booksworld.main.CustomUtils.NavigationDrawerProperties
import com.example.booksworld.main.CustomUtils.SharedPrefrencesUtils
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.PropertiesObjects.ToolBarStages
import com.example.booksworld.main.Services.DownloadService
import com.example.booksworld.main.search_suggestiosn_package.ContentPropvider_Suggestions
import com.example.booksworld.main.search_suggestiosn_package.SeriesListDataBase
import com.example.booksworld.main.search_suggestiosn_package.SuggestionsDataAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import  com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.regex.Pattern

lateinit var mNumberPicker:NumberPicker
class BookReader : AppCompatActivity(), NavigationDrawerProperties,NoticeDialogListener {
     val BIG_FONT= "${5}"
     val HUGE_FONT="${6}"
     val SMALL_FONT="${4}"
    var fontSize= "$BIG_FONT"
    var fontFamily = "Arial"
    var backgroundColor="E0E0E0"
    var textColor="616161"
    lateinit var mNavigationView:NavigationView
    lateinit var mDrawerLayout:DrawerLayout
    lateinit var mToolbar: MaterialToolbar
    var toolBareStage = ToolBarStages.Explore
    lateinit var mDataAdapter:SuggestionsDataAdapter
    val INTERNET_PERMMISION_REQUEST_CODE=30
    lateinit var mWebView:WebView
    lateinit var mCurrentPageTextView :MaterialTextView
    var mPages =ArrayList<String>()
    lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    lateinit var sp: SeriesProperties
   lateinit var imageFolder :File
    val BOOK_UID_KEY ="BOOK_UID_KEY"
    companion object{
        val TOOL_BAR_MODE ="TOOL_BAR_MODE"
        val CHANGE_BOOK_ACTION="CHANGE_BOOK_ACTION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sp = SeriesProperties.getEmptyBuild()

        initateBottomSheet()

        askPermissions()

    }


    fun initializeMainActivity(){
        mCurrentPageTextView= findViewById(R.id.current_page)

        sp=  getSeriesProperties()
        mDrawerLayout = findViewById(R.id.drawer_layout)

        mDataAdapter = SuggestionsDataAdapter(this)
        initPageConfiguration()
        initiateWebview()
        Thread(Runnable{
            loadBook(sp)
            InitiateNumberPicker()

        }).start()
        mToolbar = findViewById(R.id.toolbar)
        mNavigationView= findViewById(R.id.navigation_layout)
        if(sp.name!=""){
            mToolbar.title=(sp.name)
        }else{
            mToolbar.title=(getString(R.string.bookmark))

        }

        setSupportActionBar(mToolbar)

        NavigationDrawerHelper.getInstance(this)


        println("Load book from main page :${sp.page}")
    }

    private fun initateBottomSheet(){
        val layout = findViewById<LinearLayout>(R.id.bottom_sheet_test)
        mBottomSheetBehavior = from(layout)
        mBottomSheetBehavior.state= STATE_HIDDEN
        mBottomSheetBehavior.peekHeight=150

    }

    private fun initiateWebview(){
        println("initiateWebview:called")
            mWebView = findViewById(R.id.webview)
            mWebView.webViewClient = object:WebViewClient(){
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return true
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return true
                }

                override fun onLoadResource(view: WebView?, url: String?) {
                }
            }
        allowWebViewUses()


    }

    fun nextClick(v:View){
        println("index is:${sp.page}")
        movePage(1)
    }

    fun prevClick(v:View){
     movePage(-1)
    }

    fun movePage(pagesToPrecceed:Int){
        var tempIndex = sp.page+pagesToPrecceed
        if(tempIndex>=mPages.size){
            Toast.makeText(this,R.string.last_page,Toast.LENGTH_LONG).show()
            return
        }else if(tempIndex<0){
            Toast.makeText(this,R.string.first_page,Toast.LENGTH_LONG).show()
            return
        }
        sp.page=tempIndex
        mWebView.loadDataWithBaseURL(Uri.fromFile(imageFolder).toString()+"/",getContent(),"text/html", "UTF-8",null)

    }

    override fun onPause() {
        println("onPause: sp:${sp.uid} index:${sp.page}")
        if (sp.uid==-1){
            super.onPause()
            return
        }
        println("on pause is called metrics densicty${resources.displayMetrics.density}")
        sp.scrollX=mWebView.scrollX
        sp.scrolly=mWebView.scrollY

        sp.activity=BookReader::class.qualifiedName as String
        println("book to save:${sp.name} scrollX:${sp.scrollX}, scrollY:${sp.scrolly}, page:${sp.page}")

        //var da = StarterDataAdapter(this)
        //da.update(sp)
        var values = HashMap<String ,Any>()
        values.put(SharedPrefrencesUtils.LAST_ACTIVITY_KEY,BookReader::class.qualifiedName as Any)
        values.put(SharedPrefrencesUtils.UID_KEY,sp.uid as Any)
        SharedPrefrencesUtils.update(values)
        updateSeriesProperties(sp)
        super.onPause()
    }
    fun getSeriesProperties():SeriesProperties{
       var tempsp = intent.getParcelableExtra(SeriesProperties.SERIES_PROPERTIES_KEY) as SeriesProperties?
        println("parcable sp:${sp.name} page:${sp.page} scrollY:${tempsp!!.scrolly} ")
        return if(tempsp==null) SeriesProperties.getEmptyBuild() else tempsp
    }
        fun loadBook(sp:SeriesProperties){
            println("loadBook:called:${sp.name} book pages:${sp.page}")
        var filename = sp.name + SeriesProperties.SPLITTER + sp.uid
        if(sp.uid==-1){
            mWebView.post {

                mWebView.loadData(getContent(),"text/html", "UTF-8")
                var layout = findViewById<LinearLayout>(R.id.book_reader_buttons_container)
                layout.visibility=View.GONE

            }
            return
        }
            var mainDirectory =File(FileHelper.getMainFolder(this),filename)
            imageFolder = File(mainDirectory,DownloadService.IMAGES_DIR)
            println("imageFolder:${imageFolder.exists()}")
            if(imageFolder.exists()){
                println("image folder size:${imageFolder.listFiles().size}")
                imageFolder.listFiles().forEach {
                    println("image:${it.name}")
                }
            }
              mPages = FileHelper.getPages(mainDirectory)
                println("sp page:"+sp.page)
                println("content is:${getContent()}")
            mWebView.post {

                println("base url${Uri.fromFile(imageFolder)}")
                mWebView.loadDataWithBaseURL(Uri.fromFile(imageFolder).toString()+"/",getContent(),"text/html", "UTF-8",null)
                mWebView.scrollY=sp.scrolly
                mWebView.scrollX=sp.scrollX

            }


    }

    override fun onDestroy() {
        onPause()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            INTERNET_PERMMISION_REQUEST_CODE->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initializeMainActivity()

                }
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun askPermissions(){
        var permmisons = arrayOf(        Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE)
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.INTERNET)){

            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET),INTERNET_PERMMISION_REQUEST_CODE)
            }
        }else{
            initializeMainActivity()
        }
    }

    override fun onBackPressed() {
        if(mBottomSheetBehavior.state!= STATE_HIDDEN){
            mBottomSheetBehavior.state= STATE_HIDDEN
        }else if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START)
        }else{
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
    public fun insertData(v:View){
        var contentValues = ContentValues()
        contentValues.put(SeriesListDataBase.NAME,"tom&garilas")
        var ur = contentResolver.insert(ContentPropvider_Suggestions.CONTENT_URI,contentValues)

        contentValues = ContentValues()
        contentValues.put(SeriesListDataBase.NAME,"lioz")
        ur = contentResolver.insert(ContentPropvider_Suggestions.CONTENT_URI,contentValues)
        contentValues = ContentValues()
        contentValues.put(SeriesListDataBase.NAME,"yarden")
        ur = contentResolver.insert(ContentPropvider_Suggestions.CONTENT_URI,contentValues)

        contentValues = ContentValues()
        contentValues.put(SeriesListDataBase.NAME,"kaki")
         ur = contentResolver.insert(ContentPropvider_Suggestions.CONTENT_URI,contentValues)

        println("uri is $ur")
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.info_options,menu)


        var toogle = ActionBarDrawerToggle(this,mDrawerLayout,mToolbar,
            R.string.nav_app_bar_open_drawer_description,
            R.string.close_drawer
        )


        return super.onCreateOptionsMenu(menu)
    }

    override fun onNewIntent(intent: Intent?) {
        println("new intent")
        setIntent(intent)
        super.onNewIntent(intent)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.bigFont->changeFontSize(fontSize,resources.getStringArray(R.array.font_size_values)[2])
            R.id.smallFont->changeFontSize(fontSize,resources.getStringArray(R.array.font_size_values)[0])
            R.id.hugeFont-> changeFontSize(fontSize,resources.getStringArray(R.array.font_size_values)[3])
            R.id.medium_font->changeFontSize(fontSize,resources.getStringArray(R.array.font_size_values)[1])
        }

        return super.onOptionsItemSelected(item)
    }

    fun changeFontSize( oldValue:String,newValue:String){
        if(oldValue!=newValue){
           // sp.textSize=newValue
            var edit = PreferenceManager.getDefaultSharedPreferences(this).edit()
            edit.apply {
                putString(getString(R.string.font_size),"${newValue}")
                apply()
            }
            fontSize=newValue
            mWebView.loadDataWithBaseURL(Uri.fromFile(imageFolder).toString()+"/",getContent(),"text/html", "UTF-8",null)
        }
    }



    fun allowWebViewUses() {
        val ws = mWebView.settings
        ws.javaScriptEnabled = true
        ws.builtInZoomControls = true
        ws.displayZoomControls = false
        ws.loadWithOverviewMode=true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            try {


                val m4 =
                    WebSettings::class.java.getMethod(
                        "setAppCacheMaxSize",
                        *arrayOf<Class<*>>(java.lang.Long.TYPE)
                    )
                m4.invoke(ws, 1024 * 1024 * 8)
                val m5 =
                    WebSettings::class.java.getMethod(
                        "setAppCachePath", *arrayOf<Class<*>>(
                            String::class.java
                        )
                    )
                m5.invoke(ws, "/data/data/${packageName}/cache/")
                val m6 =
                    WebSettings::class.java.getMethod(
                        "setAppCacheEnabled",
                        *arrayOf<Class<*>>(java.lang.Boolean.TYPE)
                    )
                m6.invoke(ws, java.lang.Boolean.TRUE)
            } catch (e: NoSuchMethodException) {
            } catch (e: InvocationTargetException) {
            } catch (e: IllegalAccessException) {
            }
        }
    }
    @SuppressLint("ResourceType")
    fun initPageConfiguration(){
        var pm = PreferenceManager.getDefaultSharedPreferences(this)
         fontSize = pm.getString(getString(R.string.font_size),"5")!!
        fontFamily = pm.getString(getString(R.string.font_family),"Arial")!!
        backgroundColor=pm.getString(getString(R.string.background_color),backgroundColor)!!
        textColor=pm.getString(getString(R.string.text_color),textColor)!!
        mDrawerLayout.setBackgroundColor(Color.parseColor("#$backgroundColor"))
        if(backgroundColor==getString(R.string.black_background)){
            var buttonsLayout = findViewById<LinearLayout>(R.id.book_reader_buttons_container)

            buttonsLayout.setBackgroundColor(Color.parseColor(resources.getString(R.color.dark)))
                    mCurrentPageTextView.setTextColor(Color.parseColor(resources.getString(R.color.lightGrey)))

        }

    }
    fun getContent():String{
        var header= "<html>" +
                "<head>\n" +
                "<style>\n" +
                ".intro {\n" +
                "color: $textColor;\n" +
                "font-family:$fontFamily;\n"+
                "font-size:${fontSize.toInt()};\n"+

                "}\n" +
                ".textnotfound {\n" +
                "color: $textColor;\n" +
                "font-family:$fontFamily;\n"+
                "font-size:${30};\n"+

                "}\n" +
                "</style>"
        "</head>"
        if(mPages.size==0){
            return header+"<body><p class=\"textnotfound\">Book Not Found</p></body>"
        }
        var page = mPages[sp.page]
        var p = Pattern.compile("<br>")
        var matcher = p.matcher(page)
//&&font-family:${fontFamily}
//        color:616161;
            println("font size is:${fontSize}")


        matcher.replaceAll("(.)\\1+")
        println("cleared page is:$page")
        var content  = header+ "<body style=\"background-color:$backgroundColor;\" > <font class=\"intro\">" +
                "$page" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "<br>" +
                "</font >"+
                "</body> " +
                "</html>"
        mCurrentPageTextView.post{
            mCurrentPageTextView.text=  "p:${sp.page+1}/${mPages.size}"

        }

        return content
    }

    fun updateSeriesProperties(sp:SeriesProperties){
        if(sp.uid!=-1) {
            mDataAdapter.update(sp)
        }
    }

    override fun getClassName(): String {
        return BookReader::class.qualifiedName!!
    }

    override fun getNavigationView(): NavigationView {
        return mNavigationView
    }

    override fun getDrawerLayout(): DrawerLayout {
        return mDrawerLayout
    }

    override fun getContext(): Context {
        return this
    }

    override fun getToolbar(): Toolbar {
        return mToolbar
    }

    override fun getActivity(): Activity? {
    return this
    }

    override fun reciveSearchResult() {
        TODO("Not yet implemented")
    }



    fun InitiateNumberPicker(){
        mNumberPicker = findViewById(R.id.page_picker)
        mNumberPicker.maxValue = mPages.size
        mNumberPicker.minValue=1
        mNumberPicker.value=sp.page+1
        val pagesArr = ArrayList<String>()
        for (i in 1..mPages.size){
            pagesArr.add("$i")

        }
        var array :Array<String> = pagesArr.toTypedArray()
        mNumberPicker.setFormatter {
             val format ="p:${it}"
            format
        }
        println("array length:${array.size} array list length is:${pagesArr.size}")
        mNumberPicker.displayedValues=array

    }

    fun goToPage(v:View){

       var pagesToProcced = mNumberPicker.value - sp.page-1 // minus 1 is because the pages  from the array start from 0 and the what the users sees is 1
        movePage(pagesToProcced)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        TODO("Not yet implemented")
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        TODO("Not yet implemented")
    }

    fun SetPageOnClick(view: View) {
        if(mBottomSheetBehavior.state== STATE_EXPANDED){
            mBottomSheetBehavior.state= STATE_COLLAPSED
        }else{
            mBottomSheetBehavior.state= STATE_HALF_EXPANDED
        }
    }

}
