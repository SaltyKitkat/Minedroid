package com.b502.minedroid

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.b502.minedroid.utils.MapManager
import com.b502.minedroid.utils.MapManager.Difficulty

class GameActivity : AppCompatActivity() {
    private lateinit var difficulty: Difficulty
    private lateinit var mapManager: MapManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val btnSmile = findViewById<AppCompatButton>(R.id.btnsmile)
        btnSmile.setOnClickListener { mapManager.reset() }
        difficulty = Difficulty.entries.toTypedArray()[intent.getIntExtra("diff", 0)]
        mapManager = MapManager(this, difficulty)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        mapManager.timer.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapManager.timer.resume()
    }

    override fun onDestroy() {
        mapManager.timer.stop()
        super.onDestroy()
    }
}