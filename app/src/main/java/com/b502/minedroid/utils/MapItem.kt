package com.b502.minedroid.utils

import android.app.Activity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

class MapItem(private val context: Activity, var isMine: Boolean) {
    //地图项
    var mineCount: Int = 0
        set(value) {
            field = value
            updateText()
        }
    var viewButton: AppCompatButton = AppCompatButton(context)
    var buttonState: State = State.DEFAULT
        set(value) {
            field = value
            updateText()
        }

    private fun updateText() {
        val state = buttonState
        when (state) {
            State.DEFAULT -> {
                viewButton.text = ""
                val drawable = ContextCompat.getDrawable(
                    context, androidx.appcompat.R.drawable.abc_btn_colored_material
                )
                viewButton.background = drawable
                val tmp = viewButton.compoundDrawablePadding + 15
                viewButton.setPadding(tmp, tmp, tmp, tmp)
            }

            State.FLAGED -> {
                viewButton.text = "标"
            }

            State.OPENED -> {
                val mineCount = this.mineCount
                if (mineCount != 0) {
                    viewButton.text = mineCount.toString()
                }
                viewButton.setBackgroundColor(0x00000000)
            }

            State.MISFLAGED -> {
                viewButton.text = "X"
            }

            State.BOOM -> {
                viewButton.text = "雷"
            }
        }
    }

    enum class State {
        DEFAULT, OPENED, FLAGED, BOOM, MISFLAGED
    }
}
