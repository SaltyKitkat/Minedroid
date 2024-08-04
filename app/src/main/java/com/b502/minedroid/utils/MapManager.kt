package com.b502.minedroid.utils

import android.app.Activity
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.b502.minedroid.MyApplication
import com.b502.minedroid.R
import java.util.Locale
import java.util.Random

class MapManager(private val context: Activity, private val difficulty: GameDifficulty) {
    enum class GameDifficulty {
        EASY, MIDDLE, HARD
    }

    enum class GameState {
        WAIT, PLAYING, OVER
    }

    val width: Int
    val height: Int
    val buttonwidth: Int

    var count: Int
    var leftblock: Int
    var leftflag: Int

    var gameState: GameState = GameState.WAIT
    private var map: Array<Array<MapItem>> = Array(50) { Array(50) { MapItem(false) } }

    private val txtTime: TextView
    private val btnsmile: Button
    private val txtleftmines: TextView
    var gametime: Int

    var timeManagementMaster: TimeManagementMaster


    // should never be used on edge items
    private fun countAround(x: Int, y: Int, filter: (MapItem) -> Boolean): Int {
        val f = { it: MapItem -> if (filter(it)) 1 else 0 }
        return f(map[x - 1][y + 1]) + f(map[x][y + 1]) + f(map[x + 1][y + 1]) + f(
            map[x - 1][y]
        ) + f(map[x + 1][y]) + f(map[x - 1][y - 1]) + f(map[x][y - 1]) + f(
            map[x + 1][y - 1]
        )
    }

//    private fun doAround(x: Int, y: Int, filter: Predicate<MapItem>, f: Consumer<MapItem>) {
//    }

    //calculate minecount upon click -- try to avoid a long latency when generating map
    private fun getMineCountAt(x: Int, y: Int): Int {
        if (map[x][y].getMineCount() == 9) {
            map[x][y].setMineCount(countAround(
                x, y
            ) { obj: MapItem? -> obj!!.isMine() })
        }
        return map[x][y].getMineCount()
    }

    fun reset() {
        timeManagementMaster.stop()
        gameState = GameState.WAIT
        count = minecount[difficulty.ordinal]
        leftflag = count
        leftblock = width * height - count
        gametime = 0
        txtTime.text = "00:00"
        txtleftmines.text = leftflag.toString()
        btnsmile.text = ":)"
        //generateMap();  // initmap is what actually wanted here.
        initMap()
    }

    init {
        width = mapsize[difficulty.ordinal][0]
        height = mapsize[difficulty.ordinal][1]
        count = minecount[difficulty.ordinal]
        leftflag = count
        leftblock = width * height - count
        buttonwidth = if (this.difficulty == GameDifficulty.EASY) 40 else 25
        gametime = 0
        txtTime = context.findViewById(R.id.txtTime)
        btnsmile = context.findViewById(R.id.btnsmile)
        txtleftmines = context.findViewById(R.id.txtleftmines)

        timeManagementMaster = TimeManagementMaster(object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                gametime++
                txtTime.text = String.format(
                    Locale.ENGLISH, "%02d:%02d", (gametime / 60), (gametime % 60)
                )
            }
        }, 10)

        txtTime.text = "00:00"
        txtleftmines.text = leftflag.toString()
        btnsmile.text = ":)"
        generateButtons()
        // generateMap(); // initmap is what actually wanted here.
        initMap()
    }

    private fun getPixelsFromDp(size: Int): Int {
        val metrics = DisplayMetrics()

        context.windowManager.defaultDisplay.getMetrics(metrics)

        return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT
    }

    //init the map, to make it looks good.
    // blocks outside the map should be opened
    private fun initMap() {
        for (i in 0..width + 1) {
            for (j in 0..height + 1) {
                map[i][j].setMine(false)
                map[i][j].setButtonState(MapItem.State.OPENED)
                map[i][j].setMineCount(0)
            }
        }
        for (i in 1..width) {
            for (j in 1..height) {
                map[i][j].setButtonState(MapItem.State.DEFAULT)
                map[i][j].setMineCount(9) //an impossible value to mark that it has not been calculated yet
            }
        }
    }

    fun generateMap(
        mask_x: Int, mask_y: Int
    ) { // mask_x,mask_y is the location where player clicked, so we should avoild to put mine there
        //生成地雷编号
        val tot = width * height
        val numlist: MutableList<Int> = ArrayList()
        for (i in 0 until tot) numlist.add(i)
        val random = Random()
        var i = 0
        while (i < count) {
            val index = random.nextInt(numlist.size)
            val ind = numlist[index]
            numlist.removeAt(index)
            val x = (ind % width) + 1
            val y = (ind / width) + 1
            if (x == mask_x && y == mask_y) {
                i--
                i++
                continue
            }
            map[x][y].setMine(true)
            i++
        }
    }

    fun gameWin() {
        timeManagementMaster.stop()
        for (i in 1..width) {
            for (j in 1..height) {
                if (map[i][j].getButtonState() == MapItem.State.DEFAULT) {
                    map[i][j].setButtonState(MapItem.State.FLAGED)
                    leftflag--
                    txtleftmines.text = leftflag.toString()
                }
            }
        }
        MyApplication.Instance.sqlHelper.addRecord(difficulty, SqlHelper.getCurrentDate(), gametime)
        btnsmile.text = ":D"
        Toast.makeText(context, "游戏胜利", Toast.LENGTH_SHORT).show()
        gameState = GameState.OVER
    }

    fun gameLose() {
        timeManagementMaster.stop()
        for (i in 1..width) {
            for (j in 1..height) {
                if (map[i][j].getButtonState() == MapItem.State.FLAGED && !map[i][j].isMine()) {
                    map[i][j].setButtonState(MapItem.State.MISFLAGED)
                } else if (map[i][j].getButtonState() == MapItem.State.DEFAULT && map[i][j].isMine()) {
                    map[i][j].setButtonState(MapItem.State.BOOM)
                }
            }
        }
        btnsmile.text = ":("
        Toast.makeText(context, "游戏结束", Toast.LENGTH_SHORT).show()
        gameState = GameState.OVER
        //todo: get score
    }

    fun extendBlockAt(x: Int, y: Int) {
        if (x == 0 || y == 0) return
        if (x == width + 1 || y == height + 1) return
        if (map[x][y].getButtonState() != MapItem.State.DEFAULT) return

        if (!map[x][y].isMine()) {
            getMineCountAt(x, y)
            map[x][y].setButtonState(MapItem.State.OPENED) //set state after calculating minecount and before recursion

            openBlockAround(x, y)

            leftblock--
            if (leftblock == 0) {
                gameWin()
            }
        } else {
            gameLose()
        }
    }

    //gaoshiqing
    fun flagBlockAround(x: Int, y: Int) {
        if (x == 0 || y == 0) return
        if (x == width + 1 || y == height + 1) return

        val Count = countAround(x, y) { it: MapItem? ->
            val state = it!!.getButtonState()
            state == MapItem.State.FLAGED || state == MapItem.State.DEFAULT
        }
        val block = map[x][y]
        if (block.getMineCount() == Count) {
            for (i in x - 1..x + 1) {
                for (j in y - 1..y + 1) {
                    if (i == x && j == y) {
                        continue
                    }
                    if (map[i][j].getButtonState() == MapItem.State.DEFAULT) {
                        map[i][j].setButtonState(MapItem.State.FLAGED)
                        leftflag--
                    }
                }
            }
        }
        txtleftmines.text = leftflag.toString()
    }

    fun openBlockAround(x: Int, y: Int) {
        if (x == 0 || y == 0) return
        if (x == width + 1 || y == height + 1) return

        val mineAround = getMineCountAt(x, y)
        val flagAround = countAround(
            x, y
        ) { it: MapItem -> it.getButtonState() == MapItem.State.FLAGED }
        val defaultAround = countAround(
            x, y
        ) { it: MapItem -> it.getButtonState() == MapItem.State.DEFAULT }
        if (defaultAround != 0 && flagAround + defaultAround == mineAround) {
            // make around default -> flaged
        }
        if (mineAround == 0 || mineAround == countAround(
                x, y
            ) { it: MapItem -> it.getButtonState() == MapItem.State.FLAGED }
        ) {
            extendBlockAt(x, y - 1)
            extendBlockAt(x, y + 1)
            extendBlockAt(x - 1, y)
            extendBlockAt(x + 1, y)
            extendBlockAt(x - 1, y - 1)
            extendBlockAt(x + 1, y - 1)
            extendBlockAt(x - 1, y + 1)
            extendBlockAt(x + 1, y + 1)
        }
    }


    fun generateButtons() {
        for (i in 0..width + 1) {
            for (j in 0..height + 1) {
                map[i][j] = MapItem(false)
            }
        }

        val tmpOnclickListener = View.OnClickListener { view: View ->
            val pos = view.tag as IntArray
            val x = pos[0]
            val y = pos[1]
            when (gameState) {
                GameState.WAIT -> {
                    generateMap(x, y)
                    timeManagementMaster.start()
                    gameState = GameState.PLAYING
                    when (map[x][y]!!.getButtonState()) {
                        MapItem.State.DEFAULT -> extendBlockAt(x, y)
                        MapItem.State.OPENED -> {
                            openBlockAround(x, y)
                            flagBlockAround(x, y)
                        }
                        else -> {}
                    }
                }

                GameState.PLAYING -> when (map[x][y]!!.getButtonState()) {
                    MapItem.State.DEFAULT -> extendBlockAt(x, y)
                    MapItem.State.OPENED -> {
                        openBlockAround(x, y)
                        flagBlockAround(x, y)
                    }
                    else -> {}
                }

                GameState.OVER -> {}
            }
        }
        val tmpOnLongClickListener = OnLongClickListener { view: View ->
            if (gameState == GameState.PLAYING) {
                val pos = view.tag as IntArray
                // Toast.makeText(context,Integer.toString(pos[0])+","+Integer.toString(pos[1]),Toast.LENGTH_SHORT ).show();
                if (map[pos[0]][pos[1]].getButtonState() == MapItem.State.DEFAULT) {
                    map[pos[0]][pos[1]].setButtonState(MapItem.State.FLAGED)
                    leftflag--
                    txtleftmines.text = leftflag.toString()
                } else if (map[pos[0]][pos[1]].getButtonState() == MapItem.State.FLAGED) {
                    map[pos[0]][pos[1]].setButtonState(MapItem.State.DEFAULT)
                    leftflag++
                    txtleftmines.text = leftflag.toString()
                }
            }
            true
        }

        val parent = context.findViewById<LinearLayout>(R.id.boxLayout)
        parent.removeAllViews()
        for (j in 1..height) {
            val ln = LinearLayout(context)
            ln.orientation = LinearLayout.HORIZONTAL

            val ll = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )

            ll.gravity = Gravity.CENTER
            ln.layoutParams = ll

            for (i in 1..width) {
                val b = AppCompatButton(context)
                val lp = LinearLayout.LayoutParams(
                    getPixelsFromDp(buttonwidth - 2), getPixelsFromDp(buttonwidth + 3)
                )
                b.layoutParams = lp
                b.tag = intArrayOf(i, j)

                b.setOnClickListener(tmpOnclickListener)

                b.isLongClickable = true
                b.setOnLongClickListener(tmpOnLongClickListener)
                ln.addView(b)
                map[i][j].setViewButton(b)
            }
            parent.addView(ln)
        }
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        timeManagementMaster.stop()
    }

    companion object {
        val mapsize: Array<IntArray> =
            arrayOf(intArrayOf(9, 9), intArrayOf(16, 16), intArrayOf(16, 30))
        val minecount: IntArray = intArrayOf(10, 40, 99)
    }
}