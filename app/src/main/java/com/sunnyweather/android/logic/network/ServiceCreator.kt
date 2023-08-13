package com.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 和PlaceService接口配套的Retrofit构建器
object ServiceCreator {

    private const val BASE_URL = "https://api.caiyunapp.com/"

    private val retrofit = Retrofit.Builder()
                                    .baseUrl(BASE_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    /**
     * 泛型实化的运用
     * @return T
     */
    inline fun <reified T> create(): T = create(T::class.java)
}