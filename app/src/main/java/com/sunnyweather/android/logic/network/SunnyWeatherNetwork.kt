package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 统一的网络数据源访问入口，对所有网络请求的API进行封装。
 */
object SunnyWeatherNetwork {

    // 创建了一个PlaceService接口的动态代理对象
    private val placeService = ServiceCreator.create<PlaceService>()

    /**
     *  Kotlin协程的挂起方法声明
     *  ●   发起根据传来的参数来搜索城市数据的请求，同时当前的协程也会被阻塞住，直到服务器
     *      响应我们的请求。
     *  ●   await()方法会将解析出来的数据模型对象取出并返回，同时恢复当前协程的执行，
     *      searchPlaces()函数在得到await()函数的返回值后会将该数据再返回到上一层。
     */
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query)
                                                            .await()

    /**
     *  使用suspendCoroutine{}函数对Retrofit库中回调进行简化
     *      向retrofit2.Call接口扩展了一个await函数，这样所有返回值
     *      是Call类型的Retrofit网络请求接口就都可以直接调用await()
     *      函数了。
     *      即：拥有了Call接口对象的上下文了。
     * @receiver Call<T>
     * @return T
     */
    private suspend fun <T> Call<T>.await(): T {

        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) {
                        continuation.resume(body)
                    } else {
                        continuation.resumeWithException(
                            RuntimeException("response body is null")
                        )
                    }
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}