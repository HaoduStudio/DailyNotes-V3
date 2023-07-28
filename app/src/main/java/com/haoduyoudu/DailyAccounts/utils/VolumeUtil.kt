package com.haoduyoudu.DailyAccounts.utils

import android.app.Service
import android.content.Context
import android.media.AudioManager
import com.haoduyoudu.DailyAccounts.BaseApplication.Companion.context


/**
 * 音量工具类
 */
object VolumeUtil {
    private var mAudioManager: AudioManager = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager

    //获取最大多媒体音量
    val mediaMaxVolume: Int
        get() = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)//音量类型

    // 设置多媒体音量
    //获取当前多媒体音量
    var mediaVolume: Int
        get() = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        set(volume) {
            mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,  //音量类型
                volume, AudioManager.FLAG_PLAY_SOUND
                        or AudioManager.FLAG_SHOW_UI
            )
        }

    //获取最大通话音量
    val callMaxVolume: Int
        get() = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)

    //设置通话音量
    //获取当前通话音量
    var callVolume: Int
        get() = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        set(volume) {
            mAudioManager.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                volume, AudioManager.FLAG_PLAY_SOUND
                        or AudioManager.FLAG_SHOW_UI
            )
        }

    //获取最大系统音量
    val systemMaxVolume: Int
        get() = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)

    //获取当前系统音量
    val systemVolume: Int
        get() = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)

    //获取最大提示音量
    val alermMaxVolume: Int
        get() = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

    //设置提示音量
    //获取当前提示音量
    var alermVolume: Int
        get() = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        set(volume) {
            mAudioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                volume, AudioManager.FLAG_PLAY_SOUND
                        or AudioManager.FLAG_SHOW_UI
            )
        }

    // 关闭/打开扬声器播放
    fun setSpeakerStatus(on: Boolean) {
        if (on) { //扬声器
            mAudioManager.isSpeakerphoneOn = true
            mAudioManager.mode = AudioManager.MODE_NORMAL
        } else {
            // 设置最大音量
            val max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            mAudioManager.setStreamVolume(
                AudioManager.STREAM_VOICE_CALL,
                max,
                AudioManager.STREAM_VOICE_CALL
            )
            // 设置成听筒模式
            mAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            mAudioManager.isSpeakerphoneOn = false // 关闭扬声器
            mAudioManager.setRouting(
                AudioManager.MODE_NORMAL,
                AudioManager.ROUTE_EARPIECE,
                AudioManager.ROUTE_ALL
            )
        }
    }
}
