package com.b502.minedroid.utils

class RecordItem(private val date: String, private val time: Int) {
    override fun toString(): String {
        val time = if (time > 60) {
            (time / 60).toString() + "分" + time % 60 + "秒"
        } else {
            (time % 60).toString() + "秒"
        }
        return date + ' ' +
                "用时" + time
    }
}
