package me.marthia.avanegar.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

@SuppressLint("MissingPermission")
fun Context.isInternetAvailable(): Boolean {
    var result = false

    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.run {
        getNetworkCapabilities(cm.activeNetwork)?.run {
            result = when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    }

    return result
}

fun Context.registerNetworkCallback(onAvailable: (Network) -> Unit, onLost: (Network) -> Unit) {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.registerDefaultNetworkCallback(object :
        ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onAvailable(network)
        }

        override fun onLost(network: Network) {
            onLost(network)
        }
    })
}

fun isInternetAvailableWithSocket(): Boolean {
    return try {
        val timeoutMs = 1500
        val socket = Socket()
        val socketAddress = InetSocketAddress("8.8.8.8", 53)

        socket.connect(socketAddress, timeoutMs)
        socket.close()

        true
    } catch (e: IOException) {
        false
    }
}