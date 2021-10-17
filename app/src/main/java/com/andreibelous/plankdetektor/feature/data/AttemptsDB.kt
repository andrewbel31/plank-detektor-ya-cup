package com.andreibelous.plankdetektor.feature.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AttemptsDB(
    context: Context
) : SQLiteOpenHelper(
    context,
    "attempts.db",
    null,
    1
) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + Columns._id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Columns.date + " INTEGER,"
                + Columns.start + " INTEGER,"
                + Columns.end + " INTEGER)")

        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME");
        onCreate(db);
    }

    enum class Columns {
        _id,
        date,
        start,
        end,
    }

    companion object {

        const val TABLE_NAME = "attempts"
    }
}