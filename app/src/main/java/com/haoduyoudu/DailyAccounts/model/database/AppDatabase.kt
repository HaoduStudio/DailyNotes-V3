package com.haoduyoudu.DailyAccounts.model.database

import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.model.database.daos.NoteDao
import com.haoduyoudu.DailyAccounts.model.models.Note

@Database(version = 1, entities = [Note::class], exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {

        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(): AppDatabase {
            Log.d("AppDatabase", "Create")
            instance?.let {
                return it
            }
            return Room.databaseBuilder(BaseApplication.context,
                AppDatabase::class.java, "app_database")
                .build().apply {
                    instance = this
                }
        }
    }
}