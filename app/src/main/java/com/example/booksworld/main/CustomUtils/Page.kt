package com.example.booksworld.main.CustomUtils

class Page {
    var lines = ""
    fun addLine(str:String){
        // lines.add(str)
        if(lines==""){
            lines+=str
        }else{
            lines += " " +str

        }
    }



}