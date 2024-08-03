package com.b502.minedroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.b502.minedroid.utils.MapManager;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private MapManager mapManager;
    MapManager.GameDifficulty dif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent i = getIntent();
        dif = MapManager.GameDifficulty.values()[i.getIntExtra("diff", 0)];
        AppCompatButton btnSmile = findViewById(R.id.btnsmile);
        btnSmile.setOnClickListener(this);
        mapManager = new MapManager(this, dif);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        if (mapManager != null) {
            mapManager.reset();
        }
    }

    @Override
    protected void onPause() {
        mapManager.getTimeManagementMaster().pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapManager.getTimeManagementMaster().isHangedup()) {
            mapManager.getTimeManagementMaster().start();
        }
    }

    @Override
    protected void onDestroy() {
        mapManager.getTimeManagementMaster().stop();
        super.onDestroy();
    }
}