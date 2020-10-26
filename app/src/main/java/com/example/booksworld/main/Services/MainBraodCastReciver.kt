package com.example.booksworld.main.Services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class
MainBraodCastReciver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            DownloadService.STOP_SERVICE_ACTION->{
                context!!.stopService(Intent(context,DownloadService::class.java))
            }
        }
    }
}