package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

/**
 * ViewModel相当于逻辑层和UI层之间的一个桥梁，虽然它更偏向于逻辑层的部分，
 * 但是由于ViewModel通常和Activity或Fragment是一一对应的，因此我们还是习
 * 惯将它们放在一起。
 */
class PlaceViewModel : ViewModel() {

    private val searchLiveData = MutableLiveData<String>()

    /**
     * 对界面上显示的城市数据进行缓存，因为原则上与界面相关的数据都应该放到
     * ViewModel中，这样可以保证它们在手机屏幕发生旋转的时候不会丢失，稍后
     * 我们会在编写UI层代码的时候用到这个集合。
     */
    val placeList = ArrayList<Place>()

    // 观察searchLiveData.value这个值
    /**
     *  每当本类的searchPlaces()方法被调用的时候.switchMap()就会被执行转换，
     *  最后将仓库层返回的LiveData对象转换成可供Activity观察的LiveData对象。
     */
    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->

        // 在转换.switchMap()方法中，发起网络请求。
        Repository.searchPlaces(query)
    }

    fun searchPlaces(query: String) {
        // MutableLiveData的setValue
        searchLiveData.value = query
    }

    /**
     * 选中的城市数据的持久化操作（ViewModel中）
     *  由于仓库层中这几个接口的内部没有开启线程，因此也不必借助LiveData
     *  对象来观察数据变化，直接调用仓库层中相应的接口并返回即可。
     */
    fun savePlace(place: Place) = Repository.savePlace(place)
    fun getSavedPlace() = Repository.getSavedPlace()
    fun isPlaceSaved() = Repository.isPlaceSaved()
}