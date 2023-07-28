package com.haoduyoudu.DailyAccounts.model.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.io.Serializable


@Entity
@TypeConverters(TypeConverter::class)
data class Note(val yy: Int, val mm: Int, val dd: Int, var mood: Pair<Int, String>, val type: Int, val data: HashMap<String, String>) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

// data (V1):
// "template" (Pair as Gson (Boolean, String) -> (isUri, Path))
// "textColor"
// "noteFolder"
// "body" (Text)
// "recordPaths" (Gson p)
// "videoPaths" (Gson p)
// "imagePaths" (Gson p)

// data (V2):
// "pageSize"
// "backgroundColor"
// "noteFolder"