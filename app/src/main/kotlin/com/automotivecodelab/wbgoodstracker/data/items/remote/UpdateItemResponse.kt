package com.automotivecodelab.wbgoodstracker.data.items.remote

import com.google.gson.annotations.SerializedName

data class UpdateItemResponse(
    @SerializedName("items") val items: List<ItemRemoteModel>,
    @SerializedName("ad") val ad: AdRemoteModel?
)