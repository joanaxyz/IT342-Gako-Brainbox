package edu.cit.gako.brainbox.network

import edu.cit.gako.brainbox.BuildConfig
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
    private var apiServiceInstance: ApiService? = null

    fun init(sessionManager: SessionManager) {
        getOrCreate(sessionManager)
    }

    fun getOrCreate(sessionManager: SessionManager): ApiService {
        val existing = apiServiceInstance
        if (existing != null) {
            return existing
        }

        return synchronized(this) {
            apiServiceInstance ?: buildApiService(sessionManager).also { apiServiceInstance = it }
        }
    }

    val apiService: ApiService
        get() = checkNotNull(apiServiceInstance) {
            "RetrofitClient.init(sessionManager) must be called before accessing apiService."
        }

    private fun buildApiService(sessionManager: SessionManager): ApiService {
        val refreshApiService = createRetrofit(OkHttpClient.Builder().build())
            .create(AuthApiService::class.java)

        val authedClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager, refreshApiService))
            .build()

        return createRetrofit(authedClient).create(ApiService::class.java)
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
