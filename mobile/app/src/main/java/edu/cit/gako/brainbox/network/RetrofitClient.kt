package edu.cit.gako.brainbox.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    val apiBaseUrl: String
        get() = "${BASE_URL.removeSuffix("/")}/api"

    @Volatile
    private var apiServiceInstance: ApiService? = null

    fun init(sessionManager: SessionManager) {
        val refreshApiService = createRetrofit(OkHttpClient.Builder().build())
            .create(AuthApiService::class.java)

        val authedClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager, refreshApiService))
            .build()

        apiServiceInstance = createRetrofit(authedClient).create(ApiService::class.java)
    }

    val apiService: ApiService
        get() = checkNotNull(apiServiceInstance) {
            "RetrofitClient.init(sessionManager) must be called before accessing apiService."
        }

    private fun createRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

