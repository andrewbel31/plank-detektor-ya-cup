package com.andreibelous.plankdetektor.feature.data

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class AttemptsDataSource(
    context: Context
) {

    private val db = AttemptsDB(context)
    private val writeableDb = db.writableDatabase
    private val readableDb = db.readableDatabase

    fun saveAttempt(attempt: Attempt): Completable =
        Completable.fromAction {
            writeableDb.insert(AttemptsDB.TABLE_NAME, null, attempt.toContentValues())
        }.subscribeOn(Schedulers.io())


    fun loadAttempts(): Observable<List<Attempt>> =
        Observable.fromCallable {
            val cursor =
                readableDb.query(
                    AttemptsDB.TABLE_NAME, null, null, null, null, null, "", ""
                )

            val attempts = mutableListOf<Attempt>()

            cursor?.use {
                while (it.moveToNext()) {
                    attempts.add(it.toAttempt())
                }
            }

            attempts.toList()
        }.subscribeOn(Schedulers.io())

}

data class Attempt(
    val id: Long = 0,
    val date: Long,
    val start: Long,
    val end: Long
)

fun Attempt.toContentValues(): ContentValues =
    ContentValues().apply {
        put(AttemptsDB.Columns.date.name, date)
        put(AttemptsDB.Columns.start.name, start)
        put(AttemptsDB.Columns.end.name, end)
    }

@SuppressLint("Range")
fun Cursor.toAttempt(): Attempt {
    val id = getLong(getColumnIndex(AttemptsDB.Columns._id.name))
    val date = getLong(getColumnIndex(AttemptsDB.Columns.date.name))
    val start = getLong(getColumnIndex(AttemptsDB.Columns.start.name))
    val end = getLong(getColumnIndex(AttemptsDB.Columns.end.name))

    return Attempt(id, date, start, end)
}