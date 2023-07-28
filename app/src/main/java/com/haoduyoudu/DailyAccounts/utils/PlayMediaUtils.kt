package com.haoduyoudu.DailyAccounts.utils

import android.media.MediaPlayer

object PlayMediaUtils {
    private val mp = MediaPlayer()

    fun play(path:String){
        initMP(path)
        if(!mp.isPlaying){
            mp.start()
        }
    }
    fun pause(){
        if(mp.isPlaying)
            mp.pause()
    }

    fun stop(){
        try {
            mp.stop()
            mp.reset()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun initMP(path:String){
        mp.reset()
        mp.setDataSource(path)
        mp.prepare()
    }

    fun isPlaying():Boolean {
        return try {
            mp.isPlaying
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }

    fun clean(){
        mp.release()
    }

    fun getTime(): Int{
        return mp.duration
    }

    fun getPos() = mp.currentPosition

    fun setOnCompleteListener(func : () -> Unit) {
        mp.setOnCompletionListener {
            func()
        }
    }

    fun setOnMediaBeReadyListener(func : () -> Unit) {
        mp.setOnPreparedListener {
            func()
        }
    }
}