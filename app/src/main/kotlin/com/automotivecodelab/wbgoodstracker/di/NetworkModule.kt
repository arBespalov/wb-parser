package com.automotivecodelab.wbgoodstracker.di

import com.automotivecodelab.wbgoodstracker.BuildConfig
import com.automotivecodelab.wbgoodstracker.data.NetworkStatusListener
import com.automotivecodelab.wbgoodstracker.data.NoInternetConnectionException
import com.automotivecodelab.wbgoodstracker.data.items.remote.ServerApi
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class NetworkModule {
    @Singleton
    @Provides
    fun provideServerApi(okHttpClient: OkHttpClient): ServerApi {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.SERVER_URL)
            .build()
            .create(ServerApi::class.java)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(networkStatusListener: NetworkStatusListener): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                if (!networkStatusListener.isNetworkAvailable) throw NoInternetConnectionException()
                chain.proceed(chain.request())
            }
            .readTimeout(40, TimeUnit.SECONDS)
            .build()
    }
}
