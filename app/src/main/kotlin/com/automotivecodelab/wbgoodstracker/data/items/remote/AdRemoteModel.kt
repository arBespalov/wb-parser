package com.automotivecodelab.wbgoodstracker.data.items.remote

import com.google.gson.annotations.SerializedName

data class AdRemoteModel(
    @SerializedName("imgUrl") val imgUrl: String,
    @SerializedName("url") val url: String
)