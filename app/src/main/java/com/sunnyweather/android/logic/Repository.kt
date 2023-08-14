package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

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

    /*  /**
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


      /**
       * 刷新天气信息
       * @param lng String
       * @param lat String
       * @return LiveData<Result<Weather>>
       */
      fun refreshWeather(lng: String, lat: String) = liveData(Dispatchers.IO) {

          val result = try {
              /**
               *  创建协程作用域 - coroutineScope
               *      使用注意：
               *          1.  会继承外部的协程的作用域并创建一个子协程，即
               *              可以给任意挂起函数提供协程作用域。
               *          2.  可以保证其作用域内的所有代码和子协程在全部执
               *              行完之前，外部的协程会一直被挂起。
               *          3.  只会阻塞当前协程，不会影响其它协程，更不会影响
               *              任何线程。
               */
              coroutineScope {

                  // 可以获取执行结果的async{}协程函数
                  val deferredRealtime = async {

                      SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
                  }
                  val deferredDaily = async {

                      SunnyWeatherNetwork.getDailyWeather(lng, lat)
                  }

                  val realtimeResponse = deferredRealtime.await()
                  val dailyResponse = deferredDaily.await()
                  if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                      val weather = Weather(
                          realtimeResponse.result.realtime,
                          dailyResponse.result.daily
                      )
                      Result.success(weather)
                  } else {
                      Result.failure(
                          RuntimeException(
                              "realtime response status is ${realtimeResponse.status}" +
                                      "daily response status is ${dailyResponse.status}"
                          )
                      )
                  }
              }
          } catch (e: Exception) {
              Result.failure<Weather>(e)
          }
          emit(result)
      }*/

    /**
     * 通过fire()方法统一进行try/catch，避免每次新增的网络请求接口都要进行try/catch。
     * @param context CoroutineContext
     * @param block SuspendFunction0<Result<T>>
     *     suspend关键字表示所有传入的Lambda表达式中的代码也是拥有挂起函数上下文的。
     * @return LiveData<Result<T>>
     */
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

    // 经过fire函数优化的方法，去除了每个新增网络接口的所必须的try/catch块和emit()方法
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO){
        coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(
                    realtimeResponse.result.realtime,
                    dailyResponse.result.daily
                )
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    /**
     * 选中的城市数据的持久化操作（仓库层中）
     *      这里的实现方式并不标准，因为即使是对SharedPreferences文件进行读写的操作，
     *      也是不太建议在主线程中进行，虽然它的执行速度通常会很快。最佳的实现方式肯定还
     *      是开启一个线程来执行这些比较耗时的任务，然后通过LiveData对象进行数据返回。
     *      即，将来实际开发中一定要按照上面说的标准写法来。
     * @param place Place
     * @return Unit
     */
    fun savePlace(place: Place) = PlaceDao.savePlace(place)
    fun getSavedPlace() = PlaceDao.getSavedPlace()
    fun isPlaceSaved() = PlaceDao.isPlaceSaved()
}
