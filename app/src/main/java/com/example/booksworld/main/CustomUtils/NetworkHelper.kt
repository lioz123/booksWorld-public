package com.example.booksworld.main.CustomUtils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.booksworld.main.Firebase.FirebaseAdapter

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkHelper(var context:Context): ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        var firebaseAdapter = FirebaseAdapter(context )
        firebaseAdapter.autoLogIn()
    }

    override fun onUnavailable() {
        super.onUnavailable()
    }
}