package com.sunnyweather.android.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.forecast.*
import kotlinx.android.synthetic.main.life_index.*
import kotlinx.android.synthetic.main.now.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.asin

/**
 * 将请求得到的数据显示到Activity界面上
 */
class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy {
        ViewModelProvider(this).get(WeatherViewModel::class.java)
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 状态栏和背景图的融合
        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_weather)

        // 从intent中取出经纬度坐标和地区名称
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        // 对WeatherLiveData对象进行观察
        viewModel.weatherLiveData.observe(
            this,
            Observer { result ->
                // 当获取到服务器返回的天气数据时
                val weather = result.getOrNull()
                if (weather != null) {
                    // 调用自定义方法进行解析与展示
                    showWeatherInfo(weather)
                } else {
                    // 没有获取到就输出提示信息和在控制台打印异常
                    Toast.makeText(
                        this, "无法成功获取天气信息", Toast.LENGTH_SHORT
                    ).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
                // 隐藏下拉刷新进度条
                swipeRefresh.isRefreshing = false
            }
        )

        // 设置下拉刷新进度条颜色
        swipeRefresh.setColorSchemeColors(R.color.design_default_color_primary)
        // 提取方法：刷新一次天气信息
        refreshWeather()
        // 下拉刷新监听器
        swipeRefresh.setOnRefreshListener {
            // 当触发下拉刷新操作的时候，就刷新天气。
            refreshWeather()
        }

        // 执行一次刷新天气信息的请求
        // viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)

        // 切换城市的点击事件
        navBtn.setOnClickListener {
            // 打开滑动菜单
            drawerLayout.openDrawer(GravityCompat.START)
        }

        drawerLayout.addDrawerListener(
            // 监听DrawerLayout的状态
            object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerOpened(drawerView: View) {}
                override fun onDrawerStateChanged(newState: Int) {}

                // 当滑动菜单关闭时
                override fun onDrawerClosed(drawerView: View) {
                    val manager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    // 隐藏输入法
                    manager.hideSoftInputFromWindow(
                        drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }


            }
        )

    }

    fun refreshWeather() {
        // 执行一次刷新天气信息的请求
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        // 显示下拉刷新进度条
        swipeRefresh.isRefreshing = true
    }

    /**
     * 解析与展示天气数据，就是从Weather对象中获取数据，然后显示到相应的空间上。
     * @param weather Weather
     * @return Unit
     */
    private fun showWeatherInfo(weather: Weather) {
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTemp.text = currentTempText
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text = currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        // 处理每天的天气信息
        for (i in 0 until days) {
            // 动态加载forecast_item.xml布局并设置相应的数据
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this)
                                     .inflate(R.layout.forecast_item, forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()}~${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            // 添加到父布局中
            forecastLayout.addView(view)
        }

        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        // 索引为0的是当天的天气数据
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        // 设置完数据使ScrollView显示出来
        weatherLayout.visibility = View.VISIBLE
    }
}