package com.example.booksworld.main.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.example.booksworld.R
import com.example.booksworld.main.CustomUtils.FormCheckerHelper
import com.example.booksworld.main.Firebase.FirebaseAdapter
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    lateinit var mMaterialPassword:TextInputEditText
    lateinit var mMaterialConfirmPassword :TextInputEditText
    lateinit var mMaterialMail:TextInputEditText
    lateinit var mFirebaseAdapter: FirebaseAdapter
    val TAG ="RegisterActivity.class"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mMaterialConfirmPassword = findViewById(R.id.registerActivityConfirmPassword)
        mMaterialPassword = findViewById(R.id.registerActivityPassword)
        mMaterialMail = findViewById(R.id.registerActivityEmail)
        mFirebaseAdapter = FirebaseAdapter(this)
        onMailTextChangeListener()
        onConfirmPasswordTextChangeListener()
        onPasswordChangeListener()
    }

    private fun onConfirmPasswordTextChangeListener() {
        mMaterialConfirmPassword.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               FormCheckerHelper.comparePasswords(mMaterialPassword,mMaterialConfirmPassword)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
        }

    private fun onPasswordChangeListener(){
        mMaterialPassword.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               FormCheckerHelper.validPasswordLength(mMaterialPassword)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
    private fun onMailTextChangeListener() {
        mMaterialMail.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               FormCheckerHelper.validateEmail(mMaterialMail)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }


    fun onRegisterClick(v: View){
       if(FormCheckerHelper.checkRegister(mMaterialMail,mMaterialPassword,mMaterialConfirmPassword)){
           val intent = Intent(applicationContext,StartApp::class.java)
           println("managed to register")
           mFirebaseAdapter.register(mMaterialMail.text.toString(),mMaterialPassword.text.toString(),intent)
       }
        println("did not manage to register")
    }
    fun onClickLogInButton(v:View){
        startActivity(Intent(this, LogInActivity::class.java))
    }




    fun print(str:String){
        Log.d(TAG,str)
    }
}