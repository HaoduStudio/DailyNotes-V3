package com.haoduyoudu.DailyAccounts.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.icu.util.TimeZone
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

object DateUtils {
    private val instanceCal = getCalendar()

    private val code2Day = mapOf<Int, String>(
        0 to "Sun",
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat"
    )

    const val FORMAT_YYYY_MM_DD = "yyyyMMdd"

    fun getDayOfWeek(yy: Int, mm: Int, dd: Int): String {
        val mCal = getCalendar()
        mCal.set(Calendar.YEAR, yy)
        mCal.set(Calendar.MONTH, mm - 1)
        mCal.set(Calendar.DAY_OF_MONTH, dd)
        var week = mCal.get(Calendar.DAY_OF_WEEK) - 1
        if (week < 0) week = 0
        return code2Day[week]!!
    }

    fun getDayOfWeek(date: Date): String {
        val mCal = getCalendar()
        mCal.time = date
        var week = mCal.get(Calendar.DAY_OF_WEEK) - 1
        if (week < 0) week = 0
        return code2Day[week]!!
    }

    fun getCalendarFromFormat(str: String, format: String): Calendar {
        val date = SimpleDateFormat(format).parse(str)
        val mCal = getCalendar()
        mCal.time = date
        return mCal
    }

    fun getYYYYFromCalendar(calendar: Calendar = instanceCal) = calendar.get(Calendar.YEAR)
    fun getMMFromCalendar(calendar: Calendar = instanceCal) = calendar.get(Calendar.MONTH) + 1
    fun getDDFromCalendar(calendar: Calendar = instanceCal) = calendar.get(Calendar.DAY_OF_MONTH)

    fun getCalendar(): Calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("GMT+8:00")
    }

    fun formatYYMMDD(yy: Int, mm: Int, dd: Int, format: String): String {
        val mCal = getCalendar()
        mCal.set(Calendar.YEAR, yy)
        mCal.set(Calendar.MONTH, mm - 1)
        mCal.set(Calendar.DAY_OF_MONTH, dd)
        return SimpleDateFormat(format).format(mCal.time)
    }

    fun getMonthDays(year: Int, month: Int): Int {
        return if (month == 2) {
            if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                29
            } else {
                28
            }
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            30
        } else {
            31
        }
    }
}