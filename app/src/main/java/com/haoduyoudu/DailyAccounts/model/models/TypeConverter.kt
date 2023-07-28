package com.haoduyoudu.DailyAccounts.model.models

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TypeConverter {
    @TypeConverter
    fun jsonToModel(json: String): Pair<Int, String> {
        val typeToken = object : TypeToken<Pair<Int, String>>() {}.type
        return Gson().fromJson(json, typeToken)
    }

    @TypeConverter
    fun modelToJson(data: Pair<Int, String>): String {
        return Gson().toJson(data)
    }

    @TypeConverter
    fun jsonToModel2(json: String): HashMap<String, String> {
        val typeToken = object : TypeToken<HashMap<String, String>>() {}.type
        return Gson().fromJson(json, typeToken)
    }

    @TypeConverter
    fun modelToJson2(data: HashMap<String, String>): String {
        return Gson().toJson(data)
    }
}