package com.example.booksworld.main.CustomUtils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.Services.DownloadService
import com.example.booksworld.main.search_suggestiosn_package.SuggestionsDataAdapter
import java.io.*
import java.nio.channels.FileChannel
import java.util.*
import java.util.regex.Pattern

class FileHelper(a: Int) {
    companion object  FileHelperStatics {
        val UTF8 = "utf8"
        val BUFFER_SIZE = 1024 * 8
        fun getAllDownloadedBoods(c:Context) : ArrayList<SeriesProperties> {
            val mainFolder = getMainFolder(c)
            val splist = ArrayList<SeriesProperties>()
            val da = SuggestionsDataAdapter(c)
            if (!mainFolder.exists()) mainFolder.mkdir()
            mainFolder.listFiles().forEach { fd ->
                assertSeriesPropertiesFromFile(fd, da, splist)
            }
            return splist

        }

        private fun assertSeriesPropertiesFromFile(
            fd: File,
            da: SuggestionsDataAdapter,
            splist: ArrayList<SeriesProperties>
        ) {
            val imgfile = File(fd, DownloadService.IMAGE_KEY)
            val fdlist = fd.name.split(SeriesProperties.SPLITTER)
            var name = fdlist[0]
            val id = fdlist[1].toInt()
            val sp = da.getBook(id)
            assertBitMap(imgfile, sp)
            splist.add(sp)
        }

        private fun assertBitMap(
            imgfile: File,
            sp: SeriesProperties
        ) {
            if (imgfile.exists()) {
                val inp = FileInputStream(imgfile)
                val bitmap = BitmapFactory.decodeStream(inp)
                sp.apply {
                    this.bitmap = bitmap
                    imageloaded = true
                }
            }
        }

        fun getPages(mainFolder:File):ArrayList<String>{
            val pages =ArrayList<String>()
            val pageLettersMount=2000
            val pageFolder= File(mainFolder,DownloadService.PAGES_FOLDER)
            pageFolder.listFiles()?.forEach {file->
                assertPagesFromFile(file, pageLettersMount, pages)
            }
            println("return pages")
            return pages
        }

        private fun assertPagesFromFile(
            file: File?,
            pageLettersMount: Int,
            pages: ArrayList<String>
        ) {
            val reader = BufferedReader(FileReader(file))
            var line = reader.readLine()
            var content = line
            while (line != null) {
                line = reader.readLine()
                if (content.length < pageLettersMount) {
                    content += line
                } else {
                    var pageArr = content.chunked(pageLettersMount)
                    var changedContent = false
                    pageArr.forEachIndexed { i, page ->
                        if (i < pageArr.size - 1) {
                            pages.add(page)
                        } else {
                            content = page
                            content += line
                            changedContent = true
                        }
                    }
                    if (!changedContent) {
                        content = line
                    }
                }
            }
            reader.close()
        }

        fun getRomancesProperties(context: Context): ArrayList<SeriesProperties> {
            var assetMannager = context.assets
            var fileInputStreamReader = assetMannager.open("romance2.txt")
            var bufferedReader = BufferedReader(InputStreamReader(fileInputStreamReader))
            var splist = ArrayList<SeriesProperties>()
            bufferedReader.forEachLine { line ->
                splist.add(SeriesProperties.Builder(line))
            }

            bufferedReader.close()
            return splist

        }
        fun write(mainFolder:File, content:String):Boolean{
            val pagesFolder = File(mainFolder,DownloadService.PAGES_FOLDER)
            pagesFolder.mkdirs()
            val p: Pattern = Pattern.compile("&nbsp;")
            val matcher = p.matcher(content)
            val con= matcher.replaceAll("")
            val charsPerTextFile = 1000000*4
            val pageCount =con.length/charsPerTextFile
            val remainder = con.length%charsPerTextFile
            for(i in 0 until pageCount){
                var buf = con.substring(i*charsPerTextFile,(i+1)*charsPerTextFile).toByteArray(Charsets.UTF_8)
                var page = File(pagesFolder,"page$i")
                var rwChannel =  RandomAccessFile(page, "rw").getChannel();
                var wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, (buf.size).toLong());
                wrBuf.put(buf);
                rwChannel.close();
            }

            var buf = con.substring(pageCount*charsPerTextFile,pageCount*charsPerTextFile+remainder).toByteArray(Charsets.UTF_8)
            var page = File(pagesFolder,"remainder$pageCount")
            var rwChannel =  RandomAccessFile(page, "rw").getChannel();
            var wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, (buf.size).toLong());
            wrBuf.put(buf);
            rwChannel.close();



            return false
        }

        fun saveImages(imgDir: File,bitmaps:HashMap<String,Bitmap>) {
            bitmaps.keys.forEach { key->
                println("key is:$key")
                var bitmap = bitmaps.get(key)
                var image = File(imgDir,key)
                var out = FileOutputStream(image)
                bitmap!!.compress(Bitmap.CompressFormat.PNG,100,out)



                println("book is saved")
            }
        }

        fun saveImage(imgDir: File,bitmap:Bitmap,name:String) {
            var image=File(imgDir,name)
            var out = FileOutputStream(image)
            bitmap!!.compress(Bitmap.CompressFormat.PNG,100,out)
            println("book is saved")

        }
        fun delete(c:Context,sp:SeriesProperties){
            println("deleteSeries:${sp.getString()}")
            val dirName = sp.name+SeriesProperties.SPLITTER+sp.uid
            val mainFolder = getMainFolder(c)
            val folder = File(mainFolder,dirName)
            val files = folder.listFiles()
            if(files.isNullOrEmpty()){ return }

            val size = files.size
            for(i in 0 until size){
                println("deleted file:${files[i].name}")
                deleteRecursieve(files[i])
            }

            println("file to delete:${dirName}")
            println("folder exists:${folder.exists()}")
            folder.delete()

        }

        fun deleteRecursieve(f:File){
            if(f.isDirectory){
                f.listFiles().forEach {
                    deleteRecursieve(it)
                }
            }

            f.delete()
        }


        fun isExternalStorageWritable(): Boolean {
            return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        }

        fun getSeriesPropertiesListFromFile(file: File): List<SeriesProperties> {
            var bufferedReader = BufferedReader(FileReader(file))
           return bufferedReader.readLines().map { SeriesProperties.BuildSmallSeriesPropertiesFromLine(it) }
        }

        fun getMainFolder(c:Context):File{
            if(isExternalStorageWritable()){
                println("uses external storage")
                return File(c.externalCacheDir!!,DownloadService.MAIN_FOLDER)
            }
            println("uses internal storage")
            return File(c.filesDir, DownloadService.MAIN_FOLDER)
        }



    }



}