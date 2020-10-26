package com.example.booksworld.main.server_mannager

import android.content.Context
import android.os.ResultReceiver
import android.util.Log
import com.example.booksworld.main.Firebase.FirebaseAdapter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL


class ServerHelper {
   companion object{
       val DOMAIN = "https://bookworldserver.herokuapp.com"
       val SEARCH_BOOK_URL="${DOMAIN}/book"
       val DOWNLOAD_BOOK_LIST="$DOMAIN/downloadBookList"
       val NAME="name"
       val SKIP="skip"
       val LIMIT="limit"
       val SUCCESS_CODE=1
       val FAILED_CODE=32
        val SUCCEES_STRING="SUCCEES"
        val FAILED_STRING ="FAILED"
        fun DownloadBooksList(
            context: Context,
            downloadReciver: ResultReceiver
        ){
            print("download book print was called")
            val firebaseAdapter=FirebaseAdapter(context)
            firebaseAdapter.sendDownloadedFile(downloadReciver)
        }


       fun print(str:String){
           Log.d(ServerHelper::class.java.name,str)
       }

       fun SearchBooks(searchResult: String,skip:Int = 0,limit:Int=10): String {

           var url = URL("$SEARCH_BOOK_URL?$NAME=$searchResult&skip=$skip&limit=$limit")
           print(url)
           var reader = BufferedReader(InputStreamReader(url.openStream()))
            var str=""
           reader.forEachLine {
               str+=it
           }
            return str
       }
   }

}