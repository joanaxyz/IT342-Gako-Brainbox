package edu.cit.gako.brainbox.platform.network

import edu.cit.gako.brainbox.BuildConfig
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private val baseUrl: String
        get() = BuildConfig.BRAINBOX_API_BASE_URL
            .trim()
            .ifBlank { "http://10.0.2.2:8080/" }
            .let { candidate ->
                if (candidate.endsWith("/")) candidate else "$candidate/"
            }

    val apiBaseUrl: String
        get() = "${baseUrl.removeSuffix("/")}/api"

    @Volatile
    private var authedRetrofit: Retrofit? = null

    fun init(sessionManager: SessionManager) {
        getOrCreateRetrofit(sessionManager)
    }

    fun <T> create(serviceClass: Class<T>, sessionManager: SessionManager): T =
        getOrCreateRetrofit(sessionManager).create(serviceClass)

    private fun getOrCreateRetrofit(sessionManager: SessionManager): Retrofit {
        val existing = authedRetrofit
        if (existing != null) {
            return existing
        }

        return synchronized(this) {
            authedRetrofit ?: buildRetrofit(sessionManager).also { authedRetrofit = it }
        }
    }

    private fun buildRetrofit(sessionManager: SessionManager): Retrofit {
        val refreshApiService = createRetrofit(
            OkHttpClient.Builder()
                .dispatcher(tunedDispatcher())
                .build()
        )
            .create(TokenRefreshApiService::class.java)

        val authedClient = OkHttpClient.Builder()
            .dispatcher(tunedDispatcher())
            .addInterceptor(AuthInterceptor(sessionManager, refreshApiService))
            .build()

        return createRetrofit(authedClient)
    }

    private fun tunedDispatcher(): Dispatcher =
        Dispatcher().apply {
            maxRequests = 16
            maxRequestsPerHost = 10
        }

    private fun createRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
