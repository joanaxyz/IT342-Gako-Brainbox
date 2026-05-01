package edu.cit.gako.brainbox.platform.connectivity

import edu.cit.gako.brainbox.platform.local.model.ConnectivitySnapshot
import kotlinx.coroutines.flow.Flow

interface ConnectivityMonitor {
    val state: Flow<ConnectivitySnapshot>
    val isOnline: Flow<Boolean>
    fun currentState(): ConnectivitySnapshot
}

