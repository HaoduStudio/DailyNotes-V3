package com.haoduyoudu.DailyAccounts.view.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityDeleteSafeCheckBinding
import com.haoduyoudu.DailyAccounts.view.activities.base.DialogActivity
import rx_activity_result2.RxActivityResult

class DeleteSafeCheck : DialogActivity() {

    companion object {
        @SuppressLint("CheckResult")
        fun check(context: AppCompatActivity, func: (Boolean) -> Unit) {
            RxActivityResult.on(context).startIntent(
                Intent(context, DeleteSafeCheck::class.java)
            ).subscribe { data ->
                if (data.resultCode() == Activity.RESULT_OK) {
                    func(true)
                }else if (data.resultCode() == Activity.RESULT_CANCELED){
                    func(false)
                }
            }
        }
    }

    private val binding by lazy { ActivityDeleteSafeCheckBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.delete.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent())
            finish()
        }

        binding.cancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, Intent())
            finish()
        }
    }
}