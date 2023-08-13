package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// 访问彩云天气API的Retrofit接口
interface PlaceService {

    /**
     * 发起GET请求，观察API发现只有query这个参数是需要动态指定的，其它基本不会变的
     * 就写成固定方式就好了。
     * @param query String
     * @return Call<PlaceResponse>  服务器返回的数据将会被解析为PlaceResponse数据类对象
     */
    @GET("v2/place?token=${SunnyWeatherApplication.caiYunTOKEN}&lang=zh_CN")
    fun searchPlaces(@Query("query") query: String): Call<PlaceResponse>
}
