package com.haoduyoudu.DailyAccounts.view.activities

import android.annotation.SuppressLint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.databinding.ActivityNoteEditBodyBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.model.listener.ChangeNoteDataCallBack
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.view.activities.base.DialogActivity
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import kotlin.concurrent.thread

class NoteEditBody : DialogActivity() {

    private val binding by lazy { ActivityNoteEditBodyBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private lateinit var note: Note
    private var textColorIdNow = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val noteId = intent.getLongExtra("noteId", -1L)
        if (noteId == -1L) {
            makeToast("Empty id")
            finish()
        }else {
            appViewModel.getNoteFromIdLiveData(noteId).observe(this) {
                if (!::note.isInitialized) {
                    try {
                        changeText(it.data["body"]!!)
                        changeTextColor(it.data["textColor"]!!.toInt())
                    }catch (e: Exception) {
                        e.printStackTrace()
                        makeToast("Loaded failure")
                        finish()
                    }
                }else {
                    try {
                        if (textColorIdNow != it.data["textColor"]!!.toInt()) {
                            changeTextColor(it.data["textColor"]!!.toInt())
                        }
                    }catch (e: Exception) {
                        e.printStackTrace()
                        makeToast("Updated failure")
                    }
                }
                note = it
            }
        }

        try {
            binding.editText.typeface = Typeface.createFromFile("/system/fonts/DroidSansMono.ttf")
            val allChild = getAllChildViews(binding.colorsBoard)
            for (i in 1..allChild.size) {
                allChild[i - 1].setOnClickListener {
                    closeColorBoard()
                    changeTextColorIntoDataBase(i)
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }

        binding.enter.setOnClickListener {
            try {
                val lastSel = binding.editText.selectionStart
                if (binding.editText.length() != lastSel) binding.editText.setText(
                    binding.editText.text.subSequence(0, lastSel).toString()
                        + "\n"
                        + binding.editText.text.subSequence(lastSel, binding.editText.length()).toString()
                )else binding.editText.setText(binding.editText.text.toString() + "\n")
                binding.editText.setSelection(lastSel+1)
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.quit.setOnClickListener {
            finish()
        }

        binding.complete.setOnClickListener {
            changeTextIntoDataBase(binding.editText.text.toString())
            finish()
        }

        binding.changeColor.setOnClickListener {
            openColorBoard()
        }
    }

    private fun openColorBoard() {
        binding.colorsBoard.visibility = View.VISIBLE
    }

    private fun closeColorBoard() {
        binding.colorsBoard.visibility = View.GONE
    }

    private fun changeText(text: String) {
        binding.editText.setText(text)
    }

    private fun changeTextColor(id: Int) {
        binding.editText.setTextColor(
            resources.getColor(BaseApplication.idToTextColor[id]!!)
        )
    }

    private fun changeTextIntoDataBase(text: String) {
        thread {
            appViewModel.changeNoteDataFromId(note.id, object : ChangeNoteDataCallBack {
                override fun doChange(it: Note) {
                    it.data["body"] = text
                }

                override fun onChangeSuccessful() {}

                override fun onChangeFailure(e: Exception) {
                    e.printStackTrace()
                    makeToast("Text changed failure")
                }

            })
        }
    }

    private fun changeTextColorIntoDataBase(color: Int) {
        thread {
            appViewModel.changeNoteDataFromId(note.id, object : ChangeNoteDataCallBack {
                override fun doChange(it: Note) {
                    it.data["textColor"] = color.toString()
                }

                override fun onChangeSuccessful() {}

                override fun onChangeFailure(e: Exception) {
                    e.printStackTrace()
                    makeToast("Text color changed failure")
                }
            })
        }
    }

    private fun getAllChildViews(view: View): List<View> {
        val allChildViews: MutableList<View> = ArrayList()
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val viewChild = view.getChildAt(i) as ViewGroup
                allChildViews.add(viewChild.getChildAt(0))
                allChildViews.add(viewChild.getChildAt(1))
                allChildViews.add(viewChild.getChildAt(2))
            }
        }
        return allChildViews
    }
}