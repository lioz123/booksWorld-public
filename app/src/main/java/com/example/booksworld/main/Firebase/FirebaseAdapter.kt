package com.example.booksworld.main.Firebase

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import android.widget.Toast
import com.example.booksworld.main.DownloadRecivers.DownloadOnBooksReciver
import com.example.booksworld.main.CustomUtils.FileHelper
import com.example.booksworld.main.DownloadRecivers.DownloadReciver
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.Services.DownloadService
import com.example.booksworld.main.activities.StartApp
import com.example.booksworld.main.search_suggestiosn_package.SuggestionsDataAdapter
import com.example.booksworld.main.server_mannager.ServerHelper
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class FirebaseAdapter(val context:Context) {
    companion object {


        private val BOOKS_NODE="books_neo_collection"
        private val USER_BOOKS="mybooks"
        private val USER_BOOKS_KEY ="USER_BOOKS_KEY"
        private val BOOK_LIST_TEXT_FILE_PATH="books list/short_filtered_list.txt"
        private val USER_NODE = "users"
        private val SHARE_PREFRENCE_USER_PASSWORD_KEY = "USER_PASSWORD_SHARE_PREFERENCES6"
        private val PASSWORD_KEY="PASSWORD_KEY"
        private val EMAIL_KEY="EMAIL_KEY"
        private val FIRST_LOG_IN_KEY = "FIRST_LOG_IN"
    }


    lateinit var mSharedPreferences: SharedPreferences
    private val mFirestore= FirebaseFirestore.getInstance()
    private val  mAuth :FirebaseAuth=FirebaseAuth.getInstance()
    private val mStorage = FirebaseStorage.getInstance()
    val TAG = "FirebaseFirestore.class"


    init {
        mSharedPreferences = context.getSharedPreferences(SHARE_PREFRENCE_USER_PASSWORD_KEY, Context.MODE_PRIVATE)
    }


    fun register(email:String,password:String,intent: Intent){
        mAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {

            Toast.makeText(context,"Registration succeeded",Toast.LENGTH_LONG).show()
            updateEmailAndPassword(password, email)
            createUserDoc(email,password,intent)
        }.addOnFailureListener {
            it.printStackTrace()
            Toast.makeText(context,"Registration failed, please try again",Toast.LENGTH_LONG).show()
        }
    }

    fun removeBookFromUnDownloadedList(sp: SeriesProperties) {
        val list = mSharedPreferences.getStringSet(USER_BOOKS_KEY,null) as MutableSet<Long>?
        list?.remove(sp.uid.toLong()-1)
        mSharedPreferences.edit().putStringSet(USER_BOOKS_KEY,list as MutableSet<String>).apply()
    }

    private fun createUserDoc(email:String,password: String,intent: Intent){
        mAuth.currentUser?.let {user->
           val doc= mFirestore.document("$USER_NODE/${user.uid}")
            val map =HashMap<String,Any>()
            map.put(USER_BOOKS,HashMap<String,String>())
            map.put("uid",user.uid)
            setFirstTimeLogIn(false)
            doc.set(map).addOnSuccessListener {
                context.startActivity(intent)
            }.addOnFailureListener {
                it.printStackTrace()
                user.delete()
                Toast.makeText(context,"Registration failed, please try again",Toast.LENGTH_LONG).show()
            }
        }
    }
    fun insertBookList() {
            val da = SuggestionsDataAdapter(context)
            var snapshot=getUserDataSnapshotTask()
            snapshot?.let {
                snapshot.addOnSuccessListener {
                  val list=    it.data?.get(USER_BOOKS) as? ArrayList<String>
                    list?.let {
                        mSharedPreferences.edit().putStringSet(USER_BOOKS_KEY,list.toSet()).apply()
                    }

                }.addOnFailureListener{
                    it.printStackTrace()
                }
            }
    }

    fun getBookList(): ArrayList<SeriesProperties> {
        var list = mSharedPreferences.getStringSet(USER_BOOKS_KEY,null)
        val dataAdapter = SuggestionsDataAdapter(context)
        return dataAdapter.getFromUidList(list)
    }
    fun isUserFirstLogIn(): Boolean {
       return mSharedPreferences.getBoolean(FIRST_LOG_IN_KEY,true)
    }
    fun setFirstTimeLogIn(bool:Boolean){
        mSharedPreferences.edit().putBoolean(FIRST_LOG_IN_KEY,bool).apply()
    }
    private fun updateEmailAndPassword(password: String, email: String) {

        mSharedPreferences.edit().apply {
            putString(PASSWORD_KEY, password)
            putString(EMAIL_KEY, email).apply()
        }.apply()
    }

    fun logIn(email: String,password: String,intent: Intent){
        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
            Toast.makeText(context,"logged in",Toast.LENGTH_LONG).show()
            updateEmailAndPassword(email,password)
            context.startActivity(intent)
        }.addOnFailureListener {
            Toast.makeText(context,"Wrong credentials, please try again",Toast.LENGTH_LONG).show()
        }

    }

    fun isLoggedIn(): Boolean {
        return mAuth.currentUser!=null
    }

    fun getUserDataSnapshotTask(): Task<DocumentSnapshot>? {
      return getUserDataDocumentReference()?.get()?.addOnFailureListener {
          it.printStackTrace()
      }
    }
    fun getUserDataDocumentReference(): DocumentReference? {
        mAuth.currentUser?.let {user->
            return mFirestore.document("$USER_NODE/${user.uid}")
        }
        return null
    }
    fun autoLogIn() {
        val sharedPref = context.getSharedPreferences(SHARE_PREFRENCE_USER_PASSWORD_KEY,Context.MODE_PRIVATE)
        val email = sharedPref.getString(EMAIL_KEY,"")!!
        val password = sharedPref.getString(PASSWORD_KEY,"")!!
        if(email!=""&&password!=""){
            val intent = Intent(context,StartApp::class.java)
            logIn(email,password,intent)
        }
    }


    fun getBookListFileDownloadTask(): FileDownloadTask {
        val ref = mStorage.getReference(BOOK_LIST_TEXT_FILE_PATH)
        val file = File.createTempFile("booklist","txt")
        return ref.getFile(file)
        }

    fun sendDownloadedFile(downloadReciver: ResultReceiver) {
        val ref = mStorage.getReference(BOOK_LIST_TEXT_FILE_PATH)
      val localFile= File.createTempFile("booklist","txt")
        var bundle = Bundle()
        ref.getFile(localFile).addOnSuccessListener {
            Print("scceed to install file")

            val splist = FileHelper.getSeriesPropertiesListFromFile(localFile)
            var dataAdapter = SuggestionsDataAdapter(context)
            dataAdapter.insert(splist)
            bundle.putString(ServerHelper.SUCCEES_STRING, "success")
            downloadReciver.send(DownloadOnBooksReciver.SUCCEES_RESULT, bundle)

        }.addOnFailureListener {
            Print("failed to install file")
            it.printStackTrace()
            bundle.putString(ServerHelper.FAILED_STRING, it.javaClass.name)
            downloadReciver.send(DownloadOnBooksReciver.SUCCEES_RESULT, bundle)
        }

    }
    fun Print(str:String){
        Log.d(TAG,str)
    }

    fun getBook(uid: Int): DocumentReference {
        println("sending document:${uid-1}")
     return mFirestore.document("${BOOKS_NODE}/${uid-1}")
    }

    fun logOut() {
        mSharedPreferences.edit().putBoolean(FIRST_LOG_IN_KEY,false).apply()
        mAuth?.let {
            it.signOut()
            Toast.makeText(context,"Sign out",Toast.LENGTH_LONG).show()
        }
    }

    fun addUserBook(sp: SeriesProperties?) {
        sp?.let{sp->
            mAuth.currentUser?.let { user->
                var doc = getMyBook(user, sp)
                doc.set(sp.toMap()).addOnCompleteListener {

                }
            }
        }


    }

    private fun getMyBook(
        user: FirebaseUser,
        sp: SeriesProperties
    ) = mFirestore.document("$USER_NODE/${user.uid}/$USER_BOOKS/${sp.uid - 1}")

    fun updateUserBookList(splist:ArrayList<SeriesProperties>){
        var uidlist= splist.map { (it.uid-1)}
        var map = HashMap<String,Any>()
        map.put(USER_BOOKS,uidlist)
        getUserDataDocumentReference()?.update(map)?.addOnSuccessListener {
            println("update succees")
        }
    }
    fun remove(sp: SeriesProperties,splist:ArrayList<SeriesProperties>) {
        mAuth.currentUser?.let { firebaseUser ->
            getMyBook(firebaseUser,sp).delete()
            updateUserBookList(splist)
        }
    }

    fun downloadMyBook(
        sp: SeriesProperties,
        downloadReciver: DownloadReciver? =null
    ) {
            if(mAuth.currentUser==null){
                Toast.makeText(context,"User is not connected",Toast.LENGTH_LONG).show()
                return
            }
             getMyBook(mAuth.currentUser!!,sp).get().addOnSuccessListener {
                 println("sp before update is:${sp.getString()}")
               sp.update(it)
                 println("updtated sp is:${sp.getString()}")
               val da = SuggestionsDataAdapter(context)
                 da.update(sp)
               DownloadService.startDownloadSeries(context, sp,downloadReciver)
           }.addOnFailureListener {
               it.printStackTrace()
               Toast.makeText(context,"Unable to download book",Toast.LENGTH_SHORT).show()
           }


    }

    fun removeBookFromDidNotDownloadList(sp: SeriesProperties) {
        val set = mSharedPreferences.getStringSet(USER_BOOKS_KEY ,null)
        set?.remove(sp.uid.toString())
        mSharedPreferences.edit().putStringSet(USER_BOOKS_KEY,set).apply()
    }

    fun getUserBook(sp: SeriesProperties): Task<DocumentSnapshot>? {
        mAuth.currentUser?.let {
            return mFirestore.document("$USER_NODE/${it.uid}/${USER_BOOKS}/${sp.uid-1}").get()
        }
        return null
    }

    fun reAuthornicate(){
        autoLogIn()
    }


}