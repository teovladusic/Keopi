package com.techpuzzle.keopi.utils.connection

import android.app.Application
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class ConnectivityManager @Inject constructor(application: Application) {

    private val TAG = "ConnectivityManager"

    var shouldCheckInternet = true

    private val connectionLiveData = ConnectionLiveData(application)

    private var doesNetworkHaveInternet = MutableStateFlow(true)

    private var isNetworkAvailable = combine(
        doesNetworkHaveInternet,
        connectionLiveData.asFlow()
    ) { doesNetworkHaveInternet, connectionFlow ->
        Pair(doesNetworkHaveInternet, connectionFlow)
    }.flatMapLatest { (doesNetworkHaveInternet, connectionFlow) ->
        flowOf(doesNetworkHaveInternet.and(connectionFlow))
    }

    // observe this in ui
    val isNetworkAvailableLiveData = isNetworkAvailable.asLiveData()

    @DelicateCoroutinesApi
    fun checkIfNetworkHasInternet() = GlobalScope.launch(Dispatchers.IO) {
        while (shouldCheckInternet) {
            ping()
            delay(5000)
        }
    }

    fun ping() {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 2500)
            socket.close()
            Log.d(TAG, "PING success.")
            doesNetworkHaveInternet.value = true
        } catch (e: IOException) {
            Log.d(TAG, "ping failed")
            doesNetworkHaveInternet.value = false
        }
    }
}