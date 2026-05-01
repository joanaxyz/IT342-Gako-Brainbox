package edu.cit.gako.brainbox.platform.network

import edu.cit.gako.brainbox.platform.network.ApiEnvelope

internal fun <T> ApiEnvelope<T>.requireData(fallbackMessage: String): T {
    if (!success) {
        throw IllegalStateException(error?.message ?: fallbackMessage)
    }

    return data ?: throw IllegalStateException(fallbackMessage)
}

internal fun ApiEnvelope<*>.requireSuccess(fallbackMessage: String) {
    if (!success) {
        throw IllegalStateException(error?.message ?: fallbackMessage)
    }
}

