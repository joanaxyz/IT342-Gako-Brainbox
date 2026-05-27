package edu.cit.gako.brainbox.platform.network

import android.content.Context
import edu.cit.gako.brainbox.platform.persistence.EncryptedSessionStore
import edu.cit.gako.brainbox.platform.persistence.SessionCredentials
import edu.cit.gako.brainbox.platform.persistence.SessionStore

class SessionManager(context: Context) {
    private val sessionStore: SessionStore = EncryptedSessionStore(context.applicationContext)
    @Volatile
    private var cachedCredentials: SessionCredentials? = null

    fun saveAuthToken(token: String) {
        sessionStore.saveAccessToken(token)
        cachedCredentials = readCredentials().copy(accessToken = token)
    }

    fun fetchAuthToken(): String? {
        return readCredentials().accessToken
    }

    fun saveRefreshToken(token: String) {
        sessionStore.saveRefreshToken(token)
        cachedCredentials = readCredentials().copy(refreshToken = token)
    }

    fun fetchRefreshToken(): String? {
        return readCredentials().refreshToken
    }

    fun saveUsername(username: String) {
        sessionStore.saveUsername(username)
        cachedCredentials = readCredentials().copy(username = username)
    }

    fun fetchUsername(): String? {
        return readCredentials().username
    }

    fun replace(credentials: SessionCredentials) {
        sessionStore.save(credentials)
        cachedCredentials = credentials
    }

    fun clearSession() {
        sessionStore.clear()
        cachedCredentials = SessionCredentials()
    }

    private fun readCredentials(): SessionCredentials {
        val cached = cachedCredentials
        if (cached != null) {
            return cached
        }

        return synchronized(this) {
            cachedCredentials ?: sessionStore.read().also { credentials ->
                cachedCredentials = credentials
            }
        }
    }
}
