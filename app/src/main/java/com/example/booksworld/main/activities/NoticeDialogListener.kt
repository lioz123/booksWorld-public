package com.example.booksworld.main.activities

import androidx.fragment.app.DialogFragment

interface NoticeDialogListener {
    fun onDialogPositiveClick(dialog: DialogFragment)
    fun onDialogNegativeClick(dialog: DialogFragment)

}