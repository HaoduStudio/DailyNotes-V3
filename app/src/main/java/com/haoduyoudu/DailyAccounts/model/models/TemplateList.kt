package com.haoduyoudu.DailyAccounts.model.models

import com.google.gson.annotations.SerializedName

data class TemplateList (
    @SerializedName("template_list")
    val templateList: List<String>,

    @SerializedName("result_code")
    val code: Int?,
) {
    fun getList(): List<String> {
        if (code != 0 || code == null) {
            throw RuntimeException("Result code is 1 or null")
        }
        return templateList
    }
}