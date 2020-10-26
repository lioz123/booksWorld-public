package com.example.booksworld.main.activities

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.booksworld.R
import com.example.booksworld.main.CustomUtils.FormCheckerHelper
import com.example.booksworld.main.CustomUtils.NetworkHelper
import com.example.booksworld.main.Firebase.FirebaseAdapter
import com.google.android.material.textfield.TextInputEditText

class LogInActivity : AppCompatActivity() {
    companion object{
    val LOG_OUT_ACTION="LOG_OUT_ACTION"
    }
    lateinit var mEmailTextField:TextInputEditText
    lateinit var mPasswordTextFied:TextInputEditText
    lateinit var mFirebaseAdapter: FirebaseAdapter
    var loggingOut =false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        mFirebaseAdapter = FirebaseAdapter(this)
        mEmailTextField = findViewById(R.id.logInActivityEmail)
        mPasswordTextFied = findViewById(R.id.logInActivityPassword)
        println("intent action:${intent.action}")
        handleIntent()
    }
    fun onLogInClicked(v: View){
        if(FormCheckerHelper.checkLogIn(mEmailTextField,mPasswordTextFied)){
            val intent = Intent(this, StartApp::class.java)
            mFirebaseAdapter.logIn(mEmailTextField.text.toString(),mPasswordTextFied.text.toString(),intent)
        }
    }

    fun onRegisterButtonClciked(v:View){
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    fun handleIntent(){
        when(intent.action){
            LOG_OUT_ACTION-> logOut()
        }

    }

    private fun logOut() {
        loggingOut = true
        println("logIn is in logOut:${loggingOut}")
        mFirebaseAdapter.logOut()

    }


    override fun onStart() {
        super.onStart()
        startActivity()
    }

    private fun startActivity() {
        if (!loggingOut && mFirebaseAdapter.isLoggedIn()) {
            startStartAppActivity()
        }
    }


    private fun startStartAppActivity() {
        startActivity(Intent(this, StartApp::class.java))
    }

}