package com.haoduyoudu.DailyAccounts.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.databinding.ActivitySearchNoteBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel

class SearchNote : BaseActivity() {
    private val binding by lazy { ActivitySearchNoteBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.cancel.setOnClickListener {
            finish()
        }

        binding.complete.setOnClickListener {
            if (binding.editText.length() > 0) {
                val mIntent = Intent(this, ShowFindResult::class.java)
                val list = binding.editText.text.toString().split(" ")
                mIntent.putExtra("keyword", list.toTypedArray())
                startActivity(mIntent)
            }else {
                makeToast("请输入关键词")
            }
        }
    }
}