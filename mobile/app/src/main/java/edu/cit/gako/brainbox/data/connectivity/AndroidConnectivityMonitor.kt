package edu.cit.gako.brainbox.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresPermission
import edu.cit.gako.brainbox.data.local.model.ConnectivitySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AndroidConnectivityMonitor(context: Context) : ConnectivityMonitor {
    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val state: Flow<ConnectivitySnapshot> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(readSnapshot())
            }

            override fun onLost(network: Network) {
                trySend(readSnapshot())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(readSnapshot())
            }
        }

        trySend(readSnapshot())

        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()

    override val isOnline: Flow<Boolean> = state.map { snapshot ->
        snapshot.isConnected && snapshot.isValidated
    }.distinctUntilChanged()

    override fun currentState(): ConnectivitySnapshot = readSnapshot()

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    private fun readSnapshot(): ConnectivitySnapshot {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val isValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val transportLabel = when {
            capabilities == null -> null
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "vpn"
            else -> "other"
        }
        return ConnectivitySnapshot(
            isConnected = isConnected,
            isValidated = isValidated,
            isMetered = connectivityManager.isActiveNetworkMetered,
            transportLabel = transportLabel
        )
    }
}

