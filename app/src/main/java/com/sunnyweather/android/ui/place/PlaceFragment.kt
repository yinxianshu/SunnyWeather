package com.sunnyweather.android.ui.place


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import kotlinx.android.synthetic.main.fragment_place.*


class PlaceFragment : Fragment() {


    /**
     *  类委托属性 by，lazy是高阶函数，延迟加载；
     *      这样代码块中的代码在一开始的时候就不会执行，只有当viewModel
     *      变量首次被调用的时候，代码块中的代码才会执行，并且只会执行一次。
     *
     *      这是一种非常棒的写法，允许我们在整个类中随时使用viewModel这个变
     *      量，而完全不用关心它何时初始化、是否为空等前提条件。
     */
    val viewModel by lazy {

        ViewModelProvider(this).get(PlaceViewModel::class.java)
    }

    // 延迟初始化变量
    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Fragment的标准用法，加载了前面编写的fragment_place.xml布局
        return inflater.inflate(R.layout.fragment_place, container, false)
    }

    // 本注解第一字符串防止“this”报红，第二个字符串参数防止"notifyDataSetChanged()"报黄
    @SuppressLint("FragmentLiveDataObserve", "NotifyDataSetChanged")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 设置LayoutManager
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager

        // 设置适配器，viewModel.placeList作为数据源。
        adapter = PlaceAdapter(this, viewModel.placeList)
        recyclerView.adapter = adapter

        // 监听搜索框内容的变化情况
        searchPlaceEdit.addTextChangedListener { editable ->

            // 获得输入框内容
            val content = editable.toString()
            // 输入框内容不为空
            if (content.isNotEmpty()) {
                // 发起搜索城市的网络请求
                viewModel.searchPlaces(content)
            } else {
                // 输入框内容为空
                // 隐藏RecyclerView
                recyclerView.visibility = View.GONE
                // 显示背景图片
                bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        /**
         *  对PlaceViewModel中的placeLiveData对象进行观察，当有数据变化的时候，
         *  就会回调传入Observer接口实现中
         */
        viewModel.placeLiveData.observe(
            this, Observer { result ->
                val places = result.getOrNull()
                if (places != null) {
                    // 得到结果不为空，显示RecyclerView，隐藏背景图片；
                    recyclerView.visibility = View.VISIBLE
                    bgImageView.visibility = View.GONE
                    // 清除数据
                    viewModel.placeList.clear()
                    // 添加数据
                    viewModel.placeList.addAll(places)
                    // 通知PlaceAdapter刷新界面
                    adapter.notifyDataSetChanged()
                } else {
                    // 数据为空，提示交互信息，并打印异常在控制台。
                    Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_SHORT)
                            .show()
                    result.exceptionOrNull()
                            ?.printStackTrace()
                }
            })

    }
}