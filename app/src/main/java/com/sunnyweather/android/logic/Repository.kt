package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import java.lang.RuntimeException

/**
 * 仓库层的统一封装入口
 *
 * 仓库层的主要工作就是判断调用方请求的数据应该是从本地数据源中
 * 获取还是从网络数据源中获取，并将获得的数据返回给调用方。因此，
 * 仓库层有点像是一个数据获取与缓存的中间层，在本地没有缓存数据
 * 的情况下就去网络层获取，如果本地已经有缓存了，就直接将缓存数
 * 据返回。
 *
 * 根据需求来看，搜索城市数据的请求并没有太多缓存的必要，每次都发
 * 起网络请求去获取最新的数据即可。
 */
object Repository {

    /**
     *  一般在仓库层中定义的方法，为了能将异步获取的数据以响应式编程的
     *  方式通知给上一层，通常会返回一个LiveData对象。
     *
     *  下面代码中的liveData()函数是lifecycle-livedata-ktx库提供的
     *  一个非常强大且好用的功能，它可以自动构建并返回一个LiveData对象，
     *  然后在它的代码块中提供一个挂起函数的上下文，这样我们就可以在
     *  liveData()函数的代码块中调用任意的挂起函数了。
     *  最后使用一个emit()方法将包装的结果发射出去，这个emit()方法其实
     *  类似于调用LiveData的setValue()方法来通知数据变化，只不过这里
     *  我们无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx
     *  库提供了这样一个替代方法。
     *
     *  Dispatchers.IO保证liveData(线程参数){}代码块中的所有代码运行在子线程中
     *      ●   Android是不允许在主线程中进行网络请求的，诸如读写数据库之类
     *          的本地数据操作也是不建议在主线程中进行的，因此非常有必要在仓
     *          库层进行一次线程转换。
     * @param query String
     * @return LiveData<Result<List<Place>>>
     */
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {

        val result = try {

            // 搜索城市数据
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if (placeResponse.status == "ok") {
                // 如果是OK，使用Kotlin内置的Result.success()方法来包装获取的城市数据列表
                val places = placeResponse.places
                Result.success(places)
            } else {
                // 非OK，使用Result.failure()方法来包装一个异常信息。
                Result.failure(
                    RuntimeException("response status is ${placeResponse.status}")
                )
            }

        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }

        // 将包装的结果返回回去
        emit(result)
    }

}