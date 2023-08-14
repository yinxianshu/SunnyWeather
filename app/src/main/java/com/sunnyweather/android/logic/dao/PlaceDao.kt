package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

// 数据直接访问层Dao
object PlaceDao {

    // 存储Place对象
    fun savePlace(place: Place) {
        // 获取编辑器并存储数据
        sharedPreferences().edit {
            // 通过Gson把Place对象转换成JSON字符串再保存
            putString("place", Gson().toJson(place))
        }
    }

    // 读取Place对象
    fun getSavedPlace(): Place {

        val placeJson = sharedPreferences().getString("place", "")
        // 通过Gson把JSON字符串解析成Place对象再返回
        return Gson().fromJson(placeJson, Place::class.java)
    }

    // 判断"place"数据是否被存储
    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() = SunnyWeatherApplication.context
                             .getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)
}