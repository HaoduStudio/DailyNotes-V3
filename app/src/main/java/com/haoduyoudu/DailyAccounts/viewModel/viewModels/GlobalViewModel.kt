package com.haoduyoudu.DailyAccounts.viewModel.viewModels

import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.model.listener.AddNoteCallBack
import com.haoduyoudu.DailyAccounts.model.listener.ChangeNoteDataCallBack
import com.haoduyoudu.DailyAccounts.model.listener.DeleteNoteCallBack
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.model.models.Weather
import com.haoduyoudu.DailyAccounts.utils.FileUtils
import com.haoduyoudu.DailyAccounts.viewModel.repositories.DatabaseRepository
import com.haoduyoudu.DailyAccounts.viewModel.repositories.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class GlobalViewModel : ViewModel() {
    val notesList: LiveData<List<Note>> = DatabaseRepository.getAllNotes()
    val appBackgroundPath: MutableLiveData<String> = MutableLiveData()
    val appConfigPre by lazy { BaseApplication.context.getSharedPreferences(BaseApplication.APP_SHARED_PREFERENCES_NAME, 0) }

    init {
        appBackgroundPath.value = getAppBackgroundPath()
        if (appConfigPre.getBoolean("app_background_is_weather", false)) {
            updateWeather(true)
        }
    }

    fun changeNoteDataFromId(noteId: Long, change: ChangeNoteDataCallBack) {
        try {
            val note = DatabaseRepository.getNoteFromId(noteId)
            change.doChange(note)
            DatabaseRepository.updateNote(note)
            GlobalScope.launch(Dispatchers.Main) {
                change.onChangeSuccessful()
            }
        }catch (e: Exception) {
            GlobalScope.launch(Dispatchers.Main) {
                change.onChangeFailure(e)
            }
        }
    }

    fun getNoteFromIdLiveData(id: Long) = DatabaseRepository.getNoteFromIdLiveData(id)

    fun deleteNote(id: Long, func: DeleteNoteCallBack? = null) {
        try {
            val note = DatabaseRepository.getNoteFromId(id)
            FileUtils.delete(note.data["noteFolder"]!!)
            DatabaseRepository.deleteNote(note)
            GlobalScope.launch(Dispatchers.Main) {
                func?.onSuccessful()
            }
        }catch (e: Exception) {
            if (func != null) {
                GlobalScope.launch(Dispatchers.Main) {
                    func.onFailure(e)
                }
            }else {
                throw e
            }
        }
    }

    fun addNote(note: Note, func: AddNoteCallBack? = null) {
        try {
            var hasExists = false
            notesList.value?.forEach {
                if (it.yy == note.yy && it.mm == note.mm && it.dd == note.dd) {
                    hasExists = true
                }
            }
            if (hasExists) {
                GlobalScope.launch(Dispatchers.Main) {
                    func?.hasExist()
                }
            }else {
                val newId = DatabaseRepository.insertNote(note)
                val newDirInNote = note.data["noteFolder"]!!
                FileUtils.makeRootDirectory(newDirInNote)
                FileUtils.makeRootDirectory(newDirInNote + "record")
                FileUtils.makeRootDirectory(newDirInNote + "image")
                FileUtils.makeRootDirectory(newDirInNote + "video")
                GlobalScope.launch(Dispatchers.Main) {
                    func?.onSuccessful(newId)
                }
            }
        }catch (e: Exception) {
            if (func != null) {
                GlobalScope.launch(Dispatchers.Main) {
                    func.onFailure(e)
                }
            }else {
                throw e
            }
        }
    }

    fun setAppBackground(isWeather: Boolean, path: String) {
        appConfigPre.edit {
            putBoolean("app_background_is_weather", isWeather)
            putString("app_background_path", path)
            putLong("app_background_set_time", System.currentTimeMillis())
        }
        if (isWeather) {
            updateWeather()
            makeToast("获取天气...")
        }else {
            appBackgroundPath.value = path
        }
    }

    private fun getAppBackgroundPath(): String {
        return appConfigPre.getString("app_background_path", "")!!
    }

    private fun updateWeather(fromCache: Boolean = false) {
        if (System.currentTimeMillis() - appConfigPre.getLong("app_background_set_time", Long.MAX_VALUE) >= BaseApplication.WEATHER_REFRESH_TIME || !fromCache) {
            // refresh and post change
            NetworkRepository.getWeatherCall().enqueue(object : Callback<Weather> {
                override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                    try {
                        BaseApplication.weatherToPath[response.body()!!.getWeather()].let {
                            if (appBackgroundPath.value != it) {
                                appBackgroundPath.value = it!!
                            }
                        }
                        appConfigPre.edit {
                            putLong("app_background_set_time", System.currentTimeMillis())
                        }
                        makeToast("成功获取")
                    }catch (e: Exception) {
                        makeToast("Error weather format")
                        e.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<Weather>, t: Throwable) {
                    t.printStackTrace()
                    makeToast("加载天气失败")
                }
            })
        }
    }
}