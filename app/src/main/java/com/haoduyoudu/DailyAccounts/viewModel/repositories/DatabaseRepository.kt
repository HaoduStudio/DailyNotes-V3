package com.haoduyoudu.DailyAccounts.viewModel.repositories

import com.haoduyoudu.DailyAccounts.model.database.AppDatabase
import com.haoduyoudu.DailyAccounts.model.models.Note

object DatabaseRepository {
    private const val tag = "Repository"

    private val appDatabase = AppDatabase.getDatabase()
    private val noteDao = appDatabase.noteDao()

    fun getAllNotes() = noteDao.loadAllNote()
    fun insertNote(res: Note) = noteDao.insertNote(res)
    fun getNoteFromIdLiveData(id: Long) = noteDao.loadNoteFromIdLiveData(id)
    fun getNoteFromId(id: Long) = noteDao.loadNoteFromId(id)
    fun updateNote(res: Note) = noteDao.updateNote(res)
    fun deleteNote(res: Note) = noteDao.deleteNote(res)
}