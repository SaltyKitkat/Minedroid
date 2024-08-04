package com.b502.minedroid.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.b502.minedroid.MyApplication;
import com.b502.minedroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;

public class MapManager {
    public enum GameDifficulty {
        EASY, MIDDLE, HARD
    }

    public enum GameState {
        WAIT, PLAYING, OVER
    }

    static final int[][] mapsize = {{9, 9}, {16, 16}, {16, 30}};
    static final int[] minecount = {10, 40, 99};

    int width;
    int height;
    int buttonwidth;

    int count;
    int leftblock;
    int leftflag;

    GameState gameState = GameState.WAIT;
    MapItem[][] map;
    GameDifficulty difficulty;

    Activity context;
    private final TextView txtTime;
    private final Button btnsmile;
    private final TextView txtleftmines;
    int gametime;

    TimeManagementMaster timeManagementMaster;

    // should never be used on edge items
    private int countAround(int x, int y, Function<MapItem, Boolean> f0) {
        Function<MapItem, Integer> f = f0.andThen((b) -> b ? 1 : 0);
        return f.apply(map[x - 1][y + 1]) + f.apply(map[x][y + 1]) + f.apply(map[x + 1][y + 1]) + f.apply(map[x - 1][y]) + f.apply(map[x + 1][y]) + f.apply(map[x - 1][y - 1]) + f.apply(map[x][y - 1]) + f.apply(map[x + 1][y - 1]);
    }

    //calculate minecount upon click -- try to avoid a long latency when generating map
    private int getMineCountAt(int x, int y) {
        if (map[x][y].getMineCount() == 9) {
            map[x][y].setMineCount(countAround(x, y, MapItem::isMine));
        }
        return map[x][y].getMineCount();
    }

    public TimeManagementMaster getTimeManagementMaster() {
        return timeManagementMaster;
    }

    public void reset() {
        timeManagementMaster.stop();
        gameState = GameState.WAIT;
        count = minecount[this.difficulty.ordinal()];
        leftflag = count;
        leftblock = width * height - count;
        gametime = 0;
        txtTime.setText("00:00");
        txtleftmines.setText(Integer.toString(leftflag));
        btnsmile.setText(":)");
        //generateMap();  // initmap is what actually wanted here.
        initMap();
    }

    public MapManager(Activity context, GameDifficulty difficulty) {
        map = new MapItem[50][50];
        this.context = context;
        this.difficulty = difficulty;
        width = mapsize[this.difficulty.ordinal()][0];
        height = mapsize[this.difficulty.ordinal()][1];
        count = minecount[this.difficulty.ordinal()];
        leftflag = count;
        leftblock = width * height - count;
        buttonwidth = this.difficulty == GameDifficulty.EASY ? 40 : 25;
        gametime = 0;
        txtTime = context.findViewById(R.id.txtTime);
        btnsmile = context.findViewById(R.id.btnsmile);
        txtleftmines = context.findViewById(R.id.txtleftmines);

        timeManagementMaster = new TimeManagementMaster(new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                gametime++;
                txtTime.setText(String.format(Locale.ENGLISH, "%02d:%02d", (gametime / 60), (gametime % 60)));
            }
        }, 10);

        txtTime.setText("00:00");
        txtleftmines.setText(Integer.toString(leftflag));
        btnsmile.setText(":)");
        generateButtons();
        // generateMap(); // initmap is what actually wanted here.
        initMap();
    }

    private int getPixelsFromDp(int size) {

        DisplayMetrics metrics = new DisplayMetrics();

        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;

    }

    //init the map, to make it looks good.
    // blocks outside the map should be opened
    private void initMap() {
        for (int i = 0; i <= width + 1; i++) {
            for (int j = 0; j <= height + 1; j++) {
                map[i][j].setMine(false);
                map[i][j].setButtonState(MapItem.State.OPENED);
                map[i][j].setMineCount(0);
            }
        }
        for (int i = 1; i <= width; i++) {
            for (int j = 1; j <= height; j++) {
                map[i][j].setButtonState(MapItem.State.DEFAULT);
                map[i][j].setMineCount(9);              //an impossible value to mark that it has not been calculated yet
            }
        }
    }

    void generateMap(int mask_x, int mask_y) {// mask_x,mask_y is the location where player clicked, so we should avoild to put mine there
        //生成地雷编号
        int tot = width * height;
        List<Integer> numlist = new ArrayList<>();
        for (int i = 0; i < tot; i++) numlist.add(i);
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(numlist.size());
            int ind = numlist.get(index);
            numlist.remove(index);
            int x = (ind % width) + 1;
            int y = (ind / width) + 1;
            if (x == mask_x && y == mask_y) {
                i--;
                continue;
            }
            map[x][y].setMine(true);
        }

        //droped because it cause a long latency when generating map
/*
        for (int i = 1; i <= width; i++) {
            for (int j = 1; j <= height; j++) {
                //统计非地雷块周围地雷数目
                if (!map[i][j].isMine()) {
                    map[i][j].setMineCount(
                            b2i(map[i - 1][j + 1].isMine()) +
                                    b2i(map[i][j + 1].isMine()) +
                                    b2i(map[i + 1][j + 1].isMine()) +
                                    b2i(map[i - 1][j].isMine()) +
                                    b2i(map[i + 1][j].isMine()) +
                                    b2i(map[i - 1][j - 1].isMine()) +
                                    b2i(map[i][j - 1].isMine()) +
                                    b2i(map[i + 1][j - 1].isMine())
                    );
                }
            }
        }
*/
    }

    void gameWin() {
        timeManagementMaster.stop();
        for (int i = 1; i <= width; i++) {
            for (int j = 1; j <= height; j++) {
                if (map[i][j].getButtonState() == MapItem.State.DEFAULT) {
                    map[i][j].setButtonState(MapItem.State.FLAGED);
                    leftflag--;
                    txtleftmines.setText(Integer.toString(leftflag));
                }
            }
        }
        MyApplication.Instance.sqlHelper.addRecord(difficulty, SqlHelper.getCurrentDate(), gametime);
        Toast.makeText(context, "游戏胜利", Toast.LENGTH_SHORT).show();
        gameState = GameState.OVER;
    }

    void gameLose() {
        timeManagementMaster.stop();
        for (int i = 1; i <= width; i++) {
            for (int j = 1; j <= height; j++) {
                if (map[i][j].getButtonState() == MapItem.State.FLAGED && !map[i][j].isMine()) {
                    map[i][j].setButtonState(MapItem.State.MISFLAGED);
                } else if (map[i][j].getButtonState() == MapItem.State.DEFAULT && map[i][j].isMine()) {
                    map[i][j].setButtonState(MapItem.State.BOOM);
                }
            }
        }
        btnsmile.setText(":(");
        Toast.makeText(context, "游戏结束", Toast.LENGTH_SHORT).show();
        gameState = GameState.OVER;
        //todo: get score
    }

    void extendBlockAt(int x, int y) {

        if (x == 0 || y == 0) return;
        if (x == width + 1 || y == height + 1) return;
        if (map[x][y].getButtonState() != MapItem.State.DEFAULT) return;

        if (!map[x][y].isMine()) {
            getMineCountAt(x, y);
            map[x][y].setButtonState(MapItem.State.OPENED);     //set state after calculating minecount and before recursion

            openBlockAround(x, y);

            leftblock--;
            if (leftblock == 0) {
                gameWin();
            }
        } else {
            gameLose();
        }
    }

    //gaoshiqing
    void flagBlockAround(int x, int y) {
        if (x == 0 || y == 0) return;
        if (x == width + 1 || y == height + 1) return;

        int Count = countAround(x, y, (it) -> {
            MapItem.State state = it.getButtonState();
            return state == MapItem.State.FLAGED || state == MapItem.State.DEFAULT;
        });
        MapItem block = map[x][y];
        if (block.getMineCount() == Count) {
            for (int i = x - 1; i <= x + 1; i++) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if (i == x && j == y) {
                        continue;
                    }
                    if (map[i][j].getButtonState() == MapItem.State.DEFAULT) {
                        map[i][j].setButtonState(MapItem.State.FLAGED);
                        leftflag--;
                    }
                }
            }
        }
        txtleftmines.setText(Integer.toString(leftflag));
    }

    void openBlockAround(int x, int y) {
        if (x == 0 || y == 0) return;
        if (x == width + 1 || y == height + 1) return;

        int mineAround = getMineCountAt(x, y);

        if (mineAround == 0 || mineAround == countAround(x, y, (it) -> it.getButtonState() == MapItem.State.FLAGED)) {
            extendBlockAt(x, y - 1);
            extendBlockAt(x, y + 1);
            extendBlockAt(x - 1, y);
            extendBlockAt(x + 1, y);
            extendBlockAt(x - 1, y - 1);
            extendBlockAt(x + 1, y - 1);
            extendBlockAt(x - 1, y + 1);
            extendBlockAt(x + 1, y + 1);
        }
    }


    void generateButtons() {
        for (int i = 0; i <= width + 1; i++) {
            for (int j = 0; j <= height + 1; j++) {
                map[i][j] = new MapItem(false);
            }
        }

        View.OnClickListener tmpOnclickListener = view -> {

            int[] pos = (int[]) view.getTag();
            int x = pos[0];
            int y = pos[1];
            switch (gameState) {
                case WAIT:
                    generateMap(x, y);
                    timeManagementMaster.start();
                    gameState = GameState.PLAYING;
                case PLAYING:
                    switch (map[x][y].getButtonState()) {
                        case DEFAULT:
                            extendBlockAt(x, y);
                            break;
                        case OPENED:
                            openBlockAround(x, y);
                            flagBlockAround(x, y);//gaoshiqing
                            break;
                        case FLAGED:
                            break;
                    }
                    break;
                case OVER:
                    break;
            }
            //   Toast.makeText(context,Integer.toString(pos[0])+","+Integer.toString(pos[1]),Toast.LENGTH_SHORT ).show();
        };
        View.OnLongClickListener tmpOnLongClickListener = view -> {
            if (gameState == GameState.PLAYING) {
                int[] pos = (int[]) view.getTag();
                // Toast.makeText(context,Integer.toString(pos[0])+","+Integer.toString(pos[1]),Toast.LENGTH_SHORT ).show();
                if (map[pos[0]][pos[1]].getButtonState() == MapItem.State.DEFAULT) {
                    map[pos[0]][pos[1]].setButtonState(MapItem.State.FLAGED);
                    leftflag--;
                    txtleftmines.setText(Integer.toString(leftflag));
                } else if (map[pos[0]][pos[1]].getButtonState() == MapItem.State.FLAGED) {
                    map[pos[0]][pos[1]].setButtonState(MapItem.State.DEFAULT);
                    leftflag++;
                    txtleftmines.setText(Integer.toString(leftflag));
                }
            }
            return true;
        };

        LinearLayout parent = context.findViewById(R.id.boxLayout);
        parent.removeAllViews();
        for (int j = 1; j <= height; j++) {
            LinearLayout ln = new LinearLayout(context);
            ln.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            ll.gravity = Gravity.CENTER;
            ln.setLayoutParams(ll);

            for (int i = 1; i <= width; i++) {
                AppCompatButton b = new AppCompatButton(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(getPixelsFromDp(buttonwidth - 2), getPixelsFromDp(buttonwidth + 3));
                b.setLayoutParams(lp);
                b.setTag(new int[]{i, j});
                //b.setPadding(1, 1, 1, 1);
                // if (map[i][j].isMine)b.setText("雷");
                //   else b.setText(Integer.toString(map[i][j].getMineCount()));

                b.setOnClickListener(tmpOnclickListener);

                b.setLongClickable(true);
                b.setOnLongClickListener(tmpOnLongClickListener);
                ln.addView(b);
                map[i][j].setViewButton(b);
            }
            parent.addView(ln);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        timeManagementMaster.stop();
        super.finalize();
    }
}


