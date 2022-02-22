package com.automotivecodelab.wbgoodstracker.data.items.local

import androidx.room.TypeConverter
import com.automotivecodelab.wbgoodstracker.domain.models.Info
import com.automotivecodelab.wbgoodstracker.domain.models.Sizes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//room do not works with generic types
class Converters {
    @TypeConverter
    fun fromStringToListInfo(value: String?): List<Info>{
        val gson = Gson()
        val type = object: TypeToken<List<Info>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromListInfoToString(list: List<Info>?): String{
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromStringToListSizes(value: String?): List<Sizes>{
        val gson = Gson()
        val type = object: TypeToken<List<Sizes>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromListSizesToString(list: List<Sizes>?): String{
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromStringToListStrings(value: String?): List<String>{
        val gson = Gson()
        val type = object: TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromListStringsToString(list: List<String>?): String{
        val gson = Gson()
        return gson.toJson(list)
    }
}