package com.sunnyweather.android.logic.model

/**
 * 用于封装DailyResponse和RealtimeResponse响应结果的数据模型对象类
 * @property realtime Realtime
 * @property daily Daily
 * @constructor
 */
data class Weather(
    val realtime: RealtimeResponse.Realtime, val daily: DailyResponse.Daily
)