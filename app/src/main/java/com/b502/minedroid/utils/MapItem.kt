package com.b502.minedroid.utils

import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

// TODO: add hook on set mineCount and buttonState
class MapItem(var isMine: Boolean) {
    //地图项
    var mineCount: Int = 0
        set(value) {
            field = value
            updateText()
        }
    var viewButton: AppCompatButton? = null
    var buttonState: State = State.DEFAULT
        set(value) {
            field = value
            updateText()
        }

    fun updateText() {
        val state = buttonState
        if (viewButton != null) {
            if (state == State.DEFAULT) {
                viewButton!!.text = ""
                val drawable = ContextCompat.getDrawable(
                    viewButton!!.context,
                    androidx.appcompat.R.drawable.abc_btn_colored_material
                )
                viewButton!!.background = drawable
                val tmp = viewButton!!.compoundDrawablePadding + 15
                viewButton!!.setPadding(tmp, tmp, tmp, tmp)
            } else if (state == State.FLAGED) {
                viewButton!!.text = "标"
            } else if (state == State.OPENED) {
                val mineCount = this.mineCount
                if (mineCount != 0) {
                    viewButton!!.text = mineCount.toString()
                }
                viewButton!!.setBackgroundColor(0x00000000)
            } else if (state == State.MISFLAGED) {
                viewButton!!.text = "X"
            } else if (state == State.BOOM) {
                viewButton!!.text = "雷"
            }
        }
    }

    enum class State {
        DEFAULT, OPENED, FLAGED, BOOM, MISFLAGED
    }
}
