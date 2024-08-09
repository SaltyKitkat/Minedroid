package com.b502.minedroid.utils

import android.app.Activity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

class MapItem(context: Activity, mine: Boolean) {
    //地图项
    var mineCount: Int = if (mine) 10 else 9
        set(value) {
            field = value
            if (buttonState == State.OPENED) updateText()
        }
    var viewButton: AppCompatButton? = null
    var buttonState: State = State.DEFAULT
        set(value) {
            field = value
            updateText()
        }
    val x: Int? get() = (viewButton?.tag as IntArray?)?.get(0)
    val y: Int? get() = (viewButton?.tag as IntArray?)?.get(1)
    var isMine: Boolean
        get() = mineCount == 10
        set(v) {
            if (v) mineCount = 10
        }
    val drawable = ContextCompat.getDrawable(
        context, androidx.appcompat.R.drawable.abc_btn_colored_material
    )

    private fun updateText() {
        when (buttonState) {
            State.DEFAULT -> {
                viewButton?.text = ""
                viewButton?.background = drawable
                viewButton?.also { button ->
                    val tmp = button.compoundDrawablePadding + 15
                    button.setPadding(tmp, tmp, tmp, tmp)
                }
            }

            State.FLAGED -> {
                viewButton?.text = "标"
            }

            State.OPENED -> {
                if (mineCount != 0) {
                    viewButton?.text = mineCount.toString()
                }
                viewButton?.setBackgroundColor(0x00000000)
            }

            State.MISFLAGED -> {
                viewButton?.text = "X"
            }

            State.BOOM -> {
                viewButton?.text = "雷"
            }
        }
    }

    enum class State {
        DEFAULT, OPENED, FLAGED, BOOM, MISFLAGED
    }
}
