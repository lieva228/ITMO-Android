package com.callmydd.mvvm.model

import com.squareup.moshi.Json

data class Message(
    @field:Json(name = "id") val id: String?,
    @field:Json(name = "from") val from: String?,
    @field:Json(name = "to") val to: String?,
    @field:Json(name = "data") val data: Data,
    @field:Json(name = "time") val time: String?
)

data class Data(
    @field:Json(name = "Text") val Text: SomeData?,
    @field:Json(name = "Image") val Image: SomeData?
)

data class SomeData(
    @field:Json(name = "text") val text: String?,
    @field:Json(name = "link") val link: String?
)
