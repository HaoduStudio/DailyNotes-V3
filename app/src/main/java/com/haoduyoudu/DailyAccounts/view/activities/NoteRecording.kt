package com.haoduyoudu.DailyAccounts.view.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityNoteRecordingBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.helper.toArray
import com.haoduyoudu.DailyAccounts.helper.toGson
import com.haoduyoudu.DailyAccounts.model.listener.ChangeNoteDataCallBack
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.AudioRecordManager
import com.haoduyoudu.DailyAccounts.view.activities.base.DialogActivity
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import com.hw.videoprocessor.util.PcmToWavUtil
import com.permissionx.guolindev.PermissionX
import java.io.File
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask

class NoteRecording : DialogActivity() {
    private val binding by lazy { ActivityNoteRecordingBinding.inflate(layoutInflater) }
    private lateinit var ringReceiver: RINGReceiver
    private lateinit var arm: AudioRecordManager
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private lateinit var note: Note
    private val maximumDuration = 1000*60*3
    private val mTimer = Timer()
    private lateinit var recordPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        PermissionX.init(this)
            .permissions(Manifest.permission.RECORD_AUDIO)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    arm = AudioRecordManager.getInstance()
                    binding.startOrStopRecord.isEnabled = true
                    Log.d("NoteRecording", "All permissions Granted")
                }
            }

        val noteId = intent.getLongExtra("noteId", -1L)
        if (noteId == -1L) {
            makeToast("Empty id")
            finish()
        }else {
            appViewModel.getNoteFromIdLiveData(noteId).observe(this) {
                note = it
            }
        }

        binding.startOrStopRecord.setOnClickListener {
            if (arm.isRecording) { // stop
                stopRecord()
            }else {
                startRecord()
            }
        }

        binding.timeLift.maxProgress = maximumDuration.toFloat()
        binding.tittle.post {
            binding.timeLift.progress = binding.timeLift.maxProgress
        }

        binding.close.setOnClickListener {
            finish()
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.xtc.alarmclock.action.ALARM_VIEW_SHOWING")
        intentFilter.addAction("com.xtc.videochat.start")
        intentFilter.addAction("android.intent.action.PHONE_STATE")
        ringReceiver = RINGReceiver()
        registerReceiver(ringReceiver,intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer.cancel()
        cancelRecord()
        unregisterReceiver(ringReceiver)
    }

    inner class RINGReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            cancelRecord()
            finish()
        }
    }

    private fun startRecord() {
        try {
            if (::note.isInitialized) {
                recordPath = note.data["noteFolder"] + "record/" + System.currentTimeMillis().toString() + ".wav"
                arm.startRecord(recordPath,
                    File(cacheDir, System.currentTimeMillis().toString() + ".pcm").absolutePath)
                binding.timeLift.color = Color.parseColor("#FF9800")
                binding.startOrStopRecord.setTextColor(Color.parseColor("#F44336"))
                binding.startOrStopRecord.setBackgroundResource(R.drawable.red_stroke_stop_button)
                binding.startOrStopRecord.text = "停止"
                binding.tittle.text = "正在录制"
                Glide.with(this).load(R.drawable.ic_playing_sound).into(binding.playGif)

                mTimer.schedule(timerTask {
                    runOnUiThread {
                        binding.timeLift.progress -= 200F
                        if (binding.timeLift.progress == 0f) {
                            stopRecord()
                        }
                    }
                }, 200L, 200L)
            }else {
                makeToast("Note empty")
                finish()
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelRecord() {
        try {
            if (::arm.isInitialized) {
                if (arm.isRecording) {
                    arm.cancelRecord()
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecord() {
        try {
            if (arm.isRecording) {
                binding.timeLift.color = Color.parseColor("#4CAF50")
                binding.timeLift.progress = binding.timeLift.maxProgress
                binding.startOrStopRecord.setTextColor(Color.parseColor("#4CAF50"))
                binding.startOrStopRecord.setBackgroundResource(R.drawable.green_stroke_start_button)
                binding.startOrStopRecord.text = "WAIT"
                binding.tittle.text = "READY"
                binding.startOrStopRecord.isEnabled = false
                (binding.playGif.drawable as GifDrawable).stop()
                thread {
                    arm.stopRecord()
                    appViewModel.changeNoteDataFromId(note.id, object : ChangeNoteDataCallBack {
                        override fun doChange(it: Note) {
                            if (::recordPath.isInitialized) {
                                it.data["recordPaths"] = it.data["recordPaths"]!!.toArray<String>().let {
                                    it.add(recordPath)
                                    it.toGson()
                                }
                            }
                        }

                        override fun onChangeSuccessful() {}

                        override fun onChangeFailure(e: Exception) {
                            e.printStackTrace()
                            makeToast("Save failure")
                        }

                    })
                    finish()
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
}