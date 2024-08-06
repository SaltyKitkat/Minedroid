package com.b502.minedroid.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.b502.minedroid.utils.MapManager.Difficulty
import java.text.SimpleDateFormat
import java.util.Calendar

class SqlHelper(context: Context, name: String, cursorFactory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, cursorFactory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        // TODO 创建数据库后，对数据库的操作
        //db= getWritableDatabase();
        db.execSQL("CREATE TABLE easyrecord (_id INTEGER PRIMARY KEY  AUTOINCREMENT, recordtime TEXT, costtime INTEGER);")
        db.execSQL("CREATE TABLE middlerecord (_id INTEGER PRIMARY KEY  AUTOINCREMENT, recordtime TEXT, costtime INTEGER);")
        db.execSQL("CREATE TABLE hardrecord (_id INTEGER PRIMARY KEY  AUTOINCREMENT, recordtime TEXT, costtime INTEGER);")
    }

    fun addRecord(difficulty: Difficulty, recordtime: String, costtime: Int) {
        val db = writableDatabase
        var sqlval = String.format(" (recordtime,costtime) VALUES ('%s',%s)", recordtime, costtime)
        sqlval = when (difficulty) {
            Difficulty.EASY -> "INSERT INTO easyrecord$sqlval"
            Difficulty.MIDDLE -> "INSERT INTO middlerecord$sqlval"
            Difficulty.HARD -> "INSERT INTO hardrecord$sqlval"
        }
        db.execSQL(sqlval)
    }

    fun getRecords(difficulty: Difficulty): List<RecordItem> {
        val ret: MutableList<RecordItem> = ArrayList()
        val result = when (difficulty) {
            Difficulty.EASY -> readableDatabase.rawQuery(
                "SELECT * from easyrecord ORDER BY costtime",
                null
            )

            Difficulty.MIDDLE -> readableDatabase.rawQuery(
                "SELECT * from middlerecord ORDER BY costtime",
                null
            )

            Difficulty.HARD -> readableDatabase.rawQuery(
                "SELECT * from hardrecord ORDER BY costtime",
                null
            )
        }
        result.moveToFirst()
        while (!result.isAfterLast) {
            val r = RecordItem(result.getString(1), result.getInt(2))
            ret.add(r)
            result.moveToNext()
        }
        result.close()
        return ret
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO 更改数据库版本的操作
    }

    companion object {
        val currentDate: String
            get() {
                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("MM-dd hh:mm:ss")
                return sdf.format(calendar.time)
            }
    }
}
