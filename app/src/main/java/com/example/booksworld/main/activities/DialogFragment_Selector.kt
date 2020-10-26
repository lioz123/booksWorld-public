package com.example.booksworld.main.activities

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException

class DialogFragment_Selector: DialogFragment() {
internal lateinit var listner :NoticeDialogListener
    override fun onAttach(context: Context) {
        try{
            listner = context as NoticeDialogListener
        }catch (e:ClassCastException){
            e.printStackTrace()
            throw ClassCastException(context.toString() +" must implemented notice interface")
        }

        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }
}