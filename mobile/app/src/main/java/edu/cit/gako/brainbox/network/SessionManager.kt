package edu.cit.gako.brainbox.network

import android.content.Context
import edu.cit.gako.brainbox.data.persistence.EncryptedSessionStore
import edu.cit.gako.brainbox.data.persistence.SessionCredentials
import edu.cit.gako.brainbox.data.persistence.SessionStore

class SessionManager(context: Context) {
    private val sessionStore: SessionStore = EncryptedSessionStore(context.applicationContext)

    fun saveAuthToken(token: String) {
        sessionStore.saveAccessToken(token)
    }

    fun fetchAuthToken(): String? {
        return sessionStore.read().accessToken
    }

    fun saveRefreshToken(token: String) {
        sessionStore.saveRefreshToken(token)
    }

    fun fetchRefreshToken(): String? {
        return sessionStore.read().refreshToken
    }

    fun saveUsername(username: String) {
        sessionStore.saveUsername(username)
    }

    fun fetchUsername(): String? {
        return sessionStore.read().username
    }

    fun replace(credentials: SessionCredentials) {
        sessionStore.save(credentials)
    }

    fun clearSession() {
        sessionStore.clear()
    }
}
