package com.haoduyoudu.DailyAccounts.model.listener

interface AddNoteCallBack {
    fun onSuccessful(id: Long)
    fun onFailure(e: Exception)
    fun hasExist()
}