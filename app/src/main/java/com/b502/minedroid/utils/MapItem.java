package com.b502.minedroid.utils;

import android.graphics.drawable.Drawable;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;


public class MapItem {
    public enum State {
        DEFAULT, OPENED, FLAGED, BOOM, MISFLAGED
    }

    //地图项
    boolean isMine;
    int mineCount;
    AppCompatButton viewButton;
    State buttonState = State.DEFAULT;

    public State getButtonState() {
        return buttonState;
    }

    public boolean isMine() {
        return isMine;
    }

    public int getMineCount() {
        return mineCount;
    }

    public Button getViewButton() {
        return viewButton;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public void setMineCount(int mineCount) {
        this.mineCount = mineCount;
    }

    public void setViewButton(AppCompatButton viewButton) {
        this.viewButton = viewButton;
    }

    public MapItem(boolean ismine) {
        setMine(ismine);
    }

    public void setButtonState(MapItem.State state) {
        this.buttonState = state;
        if (this.viewButton != null) {
            if (state == MapItem.State.DEFAULT) {
                this.viewButton.setText("");
                Drawable drawable = ContextCompat.getDrawable(this.getViewButton().getContext(), androidx.appcompat.R.drawable.abc_btn_colored_material);
                this.viewButton.setBackground(drawable);
                int tmp = this.viewButton.getCompoundDrawablePadding()+15;
                this.viewButton.setPadding(tmp, tmp, tmp, tmp);
            } else if (state == MapItem.State.FLAGED) {
                this.viewButton.setText("标");
            } else if (state == MapItem.State.OPENED) {
                int mineCount = this.mineCount;
                if (mineCount != 0) {
                    this.viewButton.setText(Integer.toString(mineCount));
                }
                this.viewButton.setBackgroundColor(0x00000000);
            } else if (state == State.MISFLAGED) {
                this.viewButton.setText("X");
            } else if (state == State.BOOM) {
                this.viewButton.setText("雷");
            }
        }
    }
}
