package com.haoduyoudu.DailyAccounts.model.models

import com.google.gson.annotations.SerializedName

data class StickerList(
    @SerializedName("sticker_list")
    val stickerList: List<String>,

    @SerializedName("result_code")
    val code: Int?
) {
    fun getList(): List<String> {
        if (code != 0 || code == null) {
            throw RuntimeException("Result code is 1")
        }
        return stickerList
    }
}