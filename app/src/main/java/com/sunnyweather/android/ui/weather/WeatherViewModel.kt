package com.sunnyweather.android.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location

class WeatherViewModel : ViewModel() {

    private val locationLiveData = MutableLiveData<Location>()

    // 这三个变量房子ViewModel中可以保?它们在手机屏幕发生旋转的时候不会丢失
    var locationLng = ""
    var locationLat = ""
    var placeName = ""

    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->

        // 使仓库层返回的LiveData对象就可以转换成一个可供Activity观察的LiveData对象
        Repository.refreshWeather(location.lng,location.lat)
    }

    /**
     * 刷新天气信息，并将传入的经纬度封装成Location对象赋值给LocationLiveData对象
     * @param lng String
     * @param lat String
     * @return Unit
     */
    fun refreshWeather(lng: String, lat: String) {
        locationLiveData.value = Location(lng,lat)
    }
}