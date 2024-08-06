package com.b502.minedroid

import android.app.Application
import com.b502.minedroid.utils.SqlHelper

class MyApplication : Application() {
    lateinit var sqlHelper: SqlHelper

    override fun onCreate() {
        super.onCreate()
        sqlHelper = SqlHelper(this, "records.db", null, 1)
        Instance = this
    }

    companion object {
        lateinit var Instance: MyApplication
    }
}
