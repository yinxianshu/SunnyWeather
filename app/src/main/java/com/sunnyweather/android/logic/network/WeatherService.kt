package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.DailyResponse
import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherService {

    /**
     * 获取实时天气信息
     * @param lng String
     * @param lat String
     * @return Call<RealtimeResponse>   返回经过Call包装的RealtimeResponse数据对象
     */
    @GET("v2.6/${SunnyWeatherApplication.caiYunTOKEN}/{lng},{lat}/realtime")
    fun getRealtimeWeather(
        @Path("lng") lng: String, @Path("lat") lat: String
    ): Call<RealtimeResponse>

    /**
     * 获取未来天气信息
     * @param lng String
     * @param lat String
     * @return Call<DailyResponse>  返回经过Call包装的DailyResponse数据对象
     *     @Path注解，用来传入动态的经纬度坐标数值。
     */
    @GET("v2.6/${SunnyWeatherApplication.caiYunTOKEN}/{lng},{lat}/daily")
    fun getDailyWeather(
        @Path("lng") lng: String, @Path("lat") lat: String
    ): Call<DailyResponse>
}