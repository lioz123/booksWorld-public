package com.example.booksworld.main.CustomUtils

import com.google.android.material.textfield.TextInputEditText

class FormCheckerHelper {
    companion object{

        fun checkLogIn(materialEmail:TextInputEditText, materialPassword:TextInputEditText): Boolean {

            return validPasswordLength(materialPassword) && validateEmail(materialEmail)
        }

        fun checkRegister(materialEmail:TextInputEditText, materialPassword:TextInputEditText,materialConfirmPassword:TextInputEditText): Boolean {
            return checkLogIn(materialEmail,materialPassword) && comparePasswords(materialPassword,materialConfirmPassword)
        }


        fun validPasswordLength(materialText: TextInputEditText): Boolean {
            val password = materialText.text.toString()
            if (password.length < 6) {
                materialText.error = "password should be at least 6 characters"
                return false
            }
            materialText.error=null
            return true
        }

        fun comparePasswords(passwordText:TextInputEditText,confirmPasswordText:TextInputEditText): Boolean {
            val password = passwordText.text.toString()
            val confirmPassword = confirmPasswordText.text.toString()
            if(password!=confirmPassword){
                confirmPasswordText.error="passwords don't match"
             return false
            }
            confirmPasswordText.error=null
            return true
        }

        fun validateEmail(materialText: TextInputEditText): Boolean {
           var email = materialText.text.toString()
            if(!checkValidatedEmail(email)){
                materialText.error = "please enter a valid email"
                return false
            }
            materialText.error=null
            return true

        }

        private fun checkValidatedEmail(str: CharSequence?): Boolean {
            return str!=null && android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
        }


    }
}