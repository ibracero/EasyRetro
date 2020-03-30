package com.easyretro.common

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.MutableLiveData

class EasyRetroConnectionManager(private val connectivityManager: ConnectivityManager) {

    val connectionLiveData = MutableLiveData<NetworkStatus>().apply {
        value = getNetworkStatus()
    }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            connectionLiveData.postValue(NetworkStatus.ONLINE)
        }

        override fun onLost(network: Network) {
            connectionLiveData.postValue(NetworkStatus.OFFLINE)
        }
    }

    init {
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
    }

    @Suppress("DEPRECATION")
    fun getNetworkStatus(): NetworkStatus {
        var result = NetworkStatus.OFFLINE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.run {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.ONLINE
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.ONLINE
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus.ONLINE
                        else -> NetworkStatus.OFFLINE
                    }
                }
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = NetworkStatus.ONLINE
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = NetworkStatus.ONLINE
                    }
                }
            }
        }
        return result
    }
}

enum class NetworkStatus {
    ONLINE,
    OFFLINE
}