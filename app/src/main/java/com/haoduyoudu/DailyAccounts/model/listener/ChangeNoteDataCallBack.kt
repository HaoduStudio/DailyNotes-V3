package com.haoduyoudu.DailyAccounts.model.listener

import com.haoduyoudu.DailyAccounts.model.models.Note

interface ChangeNoteDataCallBack {
    fun doChange(it: Note)
    fun onChangeSuccessful()
    fun onChangeFailure(e: Exception)
}