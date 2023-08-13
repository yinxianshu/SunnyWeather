package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication : Application() {


    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        // 彩云天气申请到的Token也放在这里方便后续的获取
        const val caiYunTOKEN = "zHBtup4nhvUna92F"
    }

    override fun onCreate() {

        super.onCreate()

        // getApplicationContext()
        context = applicationContext
    }
}