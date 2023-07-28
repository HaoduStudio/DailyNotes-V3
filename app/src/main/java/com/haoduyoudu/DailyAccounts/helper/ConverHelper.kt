package com.haoduyoudu.DailyAccounts.helper

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.haoduyoudu.DailyAccounts.view.customView.freeLayout.ObjectSaveModel

fun Boolean.toInt() = if (this) 1 else 0
fun Int.toBoolean() = this != 0
fun Any.toGson(): String = Gson().toJson(this)
fun <T> String.toArray(): ArrayList<T> {
    val typeToken = object : TypeToken<ArrayList<T>>() {}.type
    return Gson().fromJson(this, typeToken)
}
fun <A, B> String.toPair(): Pair<A, B> {
    val typeToken = object : TypeToken<Pair<A, B>>() {}.type
    return Gson().fromJson(this, typeToken)
}
fun String.safeToLong() = try {
    this.toLong()
}catch (e: Exception) {
    0
}