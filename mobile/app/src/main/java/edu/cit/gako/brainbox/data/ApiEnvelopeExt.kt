package edu.cit.gako.brainbox.data

import edu.cit.gako.brainbox.network.models.ApiEnvelope

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

