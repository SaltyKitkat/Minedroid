package com.b502.minedroid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.b502.minedroid.utils.MapManager.Difficulty

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btneasy = findViewById<Button>(R.id.btneasy)
        val btnmiddle = findViewById<Button>(R.id.btnmiddle)
        val btnhard = findViewById<Button>(R.id.btnhard)
        val btnrecord = findViewById<Button>(R.id.btnrecord)

        val onClick = { view: View ->
            val i = Intent(this, GameActivity::class.java)
            when (view.id) {
                R.id.btneasy -> {
                    i.putExtra("diff", Difficulty.EASY.ordinal)
                    startActivity(i)
                }

                R.id.btnmiddle -> {
                    i.putExtra("diff", Difficulty.MIDDLE.ordinal)
                    startActivity(i)
                }

                R.id.btnhard -> {
                    i.putExtra("diff", Difficulty.HARD.ordinal)
                    startActivity(i)
                }

                R.id.btnrecord -> {
                    startActivity(Intent(this, ToplistActivity::class.java))
                }

                else -> {}
            }
        }

        btneasy.setOnClickListener(onClick)
        btnmiddle.setOnClickListener(onClick)
        btnhard.setOnClickListener(onClick)
        btnrecord.setOnClickListener(onClick)
    }
}