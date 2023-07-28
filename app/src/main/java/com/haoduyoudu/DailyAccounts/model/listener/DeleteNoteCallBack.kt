package com.haoduyoudu.DailyAccounts.model.listener

interface DeleteNoteCallBack {
    fun onSuccessful()
    fun onFailure(e: Exception)
}