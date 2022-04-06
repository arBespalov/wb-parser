package com.automotivecodelab.wbgoodstracker.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusListener @Inject constructor(context: Context) {

    var isNetworkAvailable = false
        private set

    init {
        try {
            val connectivityManager: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager
                    .registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {

                        override fun onLost(network: Network) {
                            isNetworkAvailable = false
                            super.onLost(network)
                        }

                        // bcs onAvailable is called when adguard local vpn is on without internet
                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities
                        ) {
                            isNetworkAvailable =
                                networkCapabilities
                                .hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                networkCapabilities
                                    .hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                networkCapabilities
                                    .hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        }
                    })
            } else {
                val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
                isNetworkAvailable = activeNetwork?.isConnectedOrConnecting == true
            }
        } catch (t: Throwable) {
            isNetworkAvailable = true
        }
    }
}

class NoInternetConnectionException : IOException("No internet connection")
