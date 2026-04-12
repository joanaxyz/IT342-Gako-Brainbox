package edu.cit.gako.brainbox.data.persistence

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

data class SessionCredentials(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val username: String? = null
)

interface SessionStore {
    fun read(): SessionCredentials
    fun save(credentials: SessionCredentials)
    fun saveAccessToken(token: String?)
    fun saveRefreshToken(token: String?)
    fun saveUsername(username: String?)
    fun clear()
}

class EncryptedSessionStore(context: Context) : SessionStore {
    private val sharedPreferences = EncryptedSharedPreferences.create(
        FILE_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context.applicationContext,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun read(): SessionCredentials {
        return SessionCredentials(
            accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null),
            refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null),
            username = sharedPreferences.getString(KEY_USERNAME, null)
        )
    }

    override fun save(credentials: SessionCredentials) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, credentials.accessToken)
            .putString(KEY_REFRESH_TOKEN, credentials.refreshToken)
            .putString(KEY_USERNAME, credentials.username)
            .apply()
    }

    override fun saveAccessToken(token: String?) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    override fun saveRefreshToken(token: String?) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    override fun saveUsername(username: String?) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "brainbox_secure_session"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USERNAME = "username"
    }
}
