package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

/**
 *  本数据类文件是按照彩云API返回的JSON格式来定义的数据模型
 */
data class PlaceResponse(val status: String, val places: List<Place>)

data class Place(
    val name: String, val location: Location,

    /**
     * 由于JSON中一些字段的命名可能与Kotlin的命名规范不太一致，因此这里使用了
     * @ SerializedName注解，来让JSON字段和Kotlin类字段之间建立映射关系。
     */
    @SerializedName("formatted_address")
    val address: String
)

/**
 *
 * @property lng String 经度
 * @property lat String 纬度
 * @constructor
 */
data class Location(val lng: String, val lat: String)