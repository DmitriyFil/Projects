package com.example.a2048

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.animation.*
import android.widget.Button
import kotlinx.android.synthetic.main.activity_game.*
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private val previous = arrayListOf<String>()
    private val previousBackup = arrayListOf<String>()
    private var gameOverChecker = false
    private var winChecker = false
    private var swipeCount = 0
    private var undoEnable = false
    private var ifBestZero = true
    private var ifGameSaved = false
    private lateinit var mPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setStartText(getTiles())
        mPrefs = getSharedPreferences("prefs2048", Context.MODE_PRIVATE)
        getPrefs()
        if (!ifGameSaved) {
            setTile2(getTiles())
            setTile2(getTiles())
        }

        restart.setOnClickListener(this::restartListener)

        if(!ifBestZero){
            resetBest.visibility = View.VISIBLE
        }

        resetBest.setOnClickListener {
            if (!ifBestZero) {
                bestMenu.visibility = View.VISIBLE
            }
        }

        menuYes.setOnClickListener {
            ifBestZero = true
            resetBest.visibility = View.INVISIBLE
            best.text = score.text
            mPrefs.edit().putBoolean("ifBestZero", ifBestZero).apply()
            mPrefs.edit().putString("best", "0").apply()
            bestMenu.visibility = View.INVISIBLE
        }

        menuNo.setOnClickListener {
            bestMenu.visibility = View.INVISIBLE
        }

        undo.setOnClickListener {
            if (winChecker || !undoEnable) {
                return@setOnClickListener
            }
            swipeCount--
            setScore(swipeCount)
            gameIsNotOverYet()
            if (previous.isNotEmpty()) {
                val tiles = getTiles()
                for (b in getTiles()) {
                    val i = tiles.indexOf(b)
                    b.text = previous[i]
                    changeTileStyle(b)
                }
            }
            undoEnable = false
        }

        g2048.setOnClickListener {
            win()
        }

        game.setOnTouchListener(object : OnSwipeTouchListener(this@GameActivity) {

            override fun onSwipeTop() {
                setPreviousBackUp()
                if (swipeAction("top")) {
                    setTile2(getTiles())
                    swipeCount++
                    setScore(swipeCount)
                    undoEnable = true
                    if (!sumEnable()) {
                        gameOver()
                    }
                } else {
                    previous.clear()
                    previous.addAll(previousBackup)
                }
            }

            override fun onSwipeBottom() {
                setPreviousBackUp()
                if (swipeAction("bottom")) {
                    setTile2(getTiles())
                    swipeCount++
                    setScore(swipeCount)
                    undoEnable = true
                    if (!sumEnable()) {
                        gameOver()
                    }
                } else {
                    previous.clear()
                    previous.addAll(previousBackup)
                }
            }

            override fun onSwipeLeft() {
                setPreviousBackUp()
                if (swipeAction("left")) {
                    setTile2(getTiles())
                    swipeCount++
                    setScore(swipeCount)
                    undoEnable = true
                    if (!sumEnable()) {
                        gameOver()
                    }
                } else {
                    previous.clear()
                    previous.addAll(previousBackup)
                }
            }

            override fun onSwipeRight() {
                setPreviousBackUp()
                if (swipeAction("right")) {
                    setTile2(getTiles())
                    swipeCount++
                    setScore(swipeCount)
                    undoEnable = true
                    if (!sumEnable()) {
                        gameOver()
                    }
                } else {
                    previous.clear()
                    previous.addAll(previousBackup)
                }
            }

        })


    }

    override fun onPause() {
        super.onPause()
        savePrefs()
    }

    private fun savePrefs() {
        val mPrefsEditor = mPrefs.edit()
        for (btnName in getTilesAndTheirNames()) {
            mPrefsEditor.putString(btnName.key, btnName.value.text.toString())
        }
        mPrefsEditor.putBoolean("ifBestZero", ifBestZero)
        mPrefsEditor.putString("score", score.text.toString())
        mPrefsEditor.putString("best", best.text.toString())
        mPrefsEditor.apply()
        mPrefsEditor.commit()
    }

    private fun getPrefs() {
        for (btnName in getTilesAndTheirNames()) {
            btnName.value.text = mPrefs.getString(btnName.key, "")
            if (!ifGameSaved) {
                if (btnName.value.text.toString() != "") {
                    ifGameSaved = true
                }
            }
            changeTileStyle(btnName.value)
        }
        ifBestZero = mPrefs.getBoolean("ifBestZero", true)
        score.text = mPrefs.getString("score", "0")
        swipeCount = score.text.toString().toInt()
        best.text = mPrefs.getString("best", "0")
    }

    fun swipeAction(direction: String): Boolean {
        var ifSumWas = false
        setPrevious()
        val swipe = dir(direction)
        for (row in swipe) {
            if (row.any { b -> b.text.toString().isNotEmpty() }) {
                if (row[3].text.toString().isNotEmpty()) {
                    if (row[2].text.toString().isNotEmpty() && row[3].text == row[2].text) {
                        row[3].text = (row[3].text.toString().toInt() + row[2].text.toString().toInt()).toString()
                        row[2].text = ""
                        changeTileStyle(row[3])
                        changeTileStyle(row[2])

                        ifSumWas = true
                    } else if (row[2].text.toString().isEmpty() && row[1].text.toString().isNotEmpty() && row[3].text == row[1].text) {
                        row[3].text = (row[3].text.toString().toInt() + row[1].text.toString().toInt()).toString()
                        row[1].text = ""
                        changeTileStyle(row[3])
                        changeTileStyle(row[1])
                        ifSumWas = true
                    } else if (row[2].text.toString().isEmpty() && row[1].text.toString().isEmpty() && row[0].text.toString().isNotEmpty() && row[3].text == row[0].text) {
                        row[3].text = (row[3].text.toString().toInt() + row[0].text.toString().toInt()).toString()
                        row[0].text = ""
                        changeTileStyle(row[3])
                        changeTileStyle(row[0])
                        ifSumWas = true
                    }
                }
                if (row[2].text.toString().isNotEmpty()) {
                    if (row[1].text.toString().isNotEmpty() && row[2].text == row[1].text) {
                        if (row[3].text.isEmpty()) {
                            row[3].text = (row[2].text.toString().toInt() + row[1].text.toString().toInt()).toString()
                            row[2].text = ""
                            row[1].text = ""
                            changeTileStyle(row[3])
                            changeTileStyle(row[2])
                            changeTileStyle(row[1])
                            ifSumWas = true
                        } else {
                            row[2].text = (row[2].text.toString().toInt() + row[1].text.toString().toInt()).toString()
                            row[1].text = ""
                            changeTileStyle(row[2])
                            changeTileStyle(row[1])
                            ifSumWas = true
                        }
                    } else if (row[1].text.toString().isEmpty() && row[0].text.toString().isNotEmpty() && row[2].text == row[0].text) {
                        if (row[3].text.isEmpty()) {
                            row[3].text = (row[2].text.toString().toInt() + row[0].text.toString().toInt()).toString()
                            row[2].text = ""
                            row[0].text = ""
                            changeTileStyle(row[3])
                            changeTileStyle(row[2])
                            changeTileStyle(row[0])
                            ifSumWas = true
                        } else {
                            row[2].text = (row[2].text.toString().toInt() + row[0].text.toString().toInt()).toString()
                            row[0].text = ""
                            changeTileStyle(row[2])
                            changeTileStyle(row[0])
                            ifSumWas = true
                        }
                    } else if (row[3].text.toString().isEmpty()) {
                        row[3].text = row[2].text
                        row[2].text = ""
                        changeTileStyle(row[3])
                        changeTileStyle(row[2])
                        ifSumWas = true
                    }
                }
                if (row[1].text.toString().isNotEmpty()) {
                    if (row[0].text.toString().isNotEmpty() && row[1].text == row[0].text) {
                        if (row[2].text.isEmpty() && row[3].text.isEmpty()) {
                            row[3].text = (row[1].text.toString().toInt() + row[0].text.toString().toInt()).toString()
                            row[0].text = ""
                            row[1].text = ""
                            changeTileStyle(row[3])
                            changeTileStyle(row[0])
                            changeTileStyle(row[1])
                            ifSumWas = true
                        } else if (row[2].text.isEmpty()) {
                            row[2].text = (row[1].text.toString().toInt() + row[0].text.toString().toInt()).toString()
                            row[0].text = ""
                            row[1].text = ""
                            changeTileStyle(row[2])
                            changeTileStyle(row[0])
                            changeTileStyle(row[1])
                            ifSumWas = true
                        } else {
                            row[1].text = (row[1].text.toString().toInt() + row[0].text.toString().toInt()).toString()
                            row[0].text = ""
                            changeTileStyle(row[0])
                            changeTileStyle(row[1])
                            ifSumWas = true
                        }
                    } else if (row[2].text.isEmpty() && row[3].text.isEmpty()) {
                        row[3].text = row[1].text
                        row[1].text = ""
                        changeTileStyle(row[3])
                        changeTileStyle(row[1])
                        ifSumWas = true
                    } else if (row[2].text.isEmpty()) {
                        row[2].text = row[1].text
                        row[1].text = ""
                        changeTileStyle(row[2])
                        changeTileStyle(row[1])
                        ifSumWas = true
                    }
                }
                if (row[0].text.isNotEmpty()) {
                    if (row[1].text.isEmpty() && row[2].text.isEmpty() && row[3].text.isEmpty()) {
                        row[3].text = row[0].text
                        row[0].text = ""
                        changeTileStyle(row[0])
                        changeTileStyle(row[3])
                        ifSumWas = true
                    } else if (row[1].text.isEmpty() && row[2].text.isEmpty()) {
                        row[2].text = row[0].text
                        row[0].text = ""
                        changeTileStyle(row[0])
                        changeTileStyle(row[2])
                        ifSumWas = true
                    } else if (row[1].text.isEmpty()) {
                        row[1].text = row[0].text
                        row[0].text = ""
                        changeTileStyle(row[0])
                        changeTileStyle(row[1])
                        ifSumWas = true
                    }
                }
            }
        }
        return ifSumWas
    }

    private fun restartListener(@Suppress("UNUSED_PARAMETER") view: View) {
        notWinYet()
        gameIsNotOverYet()
        resetGameBoard(getTiles())
        setTile2(getTiles())
        setTile2(getTiles())
        swipeCount = 0
        setScore(swipeCount)
        undoEnable = false
    }

    private fun win() {
        if (score.text.toString().toInt() <= best.text.toString().toInt()) {
            ifBestZero = false
            resetBest.visibility = View.VISIBLE
            best.text = score.text
            mPrefs.edit().putBoolean("ifBestZero", ifBestZero).apply()
            mPrefs.edit().putString("best", best.text.toString()).apply()
        }
        winChecker = true
        win.visibility = View.VISIBLE
    }

    private fun notWinYet() {
        if (winChecker) {
            winChecker = false
            win.visibility = View.INVISIBLE
        }
    }

    private fun gameOver() {
        gameOver.visibility = View.VISIBLE
        gameOverChecker = true
    }

    private fun gameIsNotOverYet() {
        if (gameOverChecker) {
            gameOverChecker = false
            gameOver.visibility = View.INVISIBLE
        }
    }

    fun setTile2(tiles: Array<Button>): Button {
        val tile = randomFreeTile(tiles)
        val chance: Int = Random.nextInt(100)
        if (chance < 10) {
            tile.text = "4"
            changeTileStyle(tile)
        } else {
            tile.text = "2"
            tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t2))
        }
        tile.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale2tile))
        return tile
    }

    private fun setPreviousBackUp() {
        previousBackup.clear()
        previousBackup.addAll(previous)
    }

    private fun setPrevious() {
        previous.clear()
        for (b in getTiles()) {
            previous.add(b.text.toString())
        }
    }

    private fun setScore(swipeCount: Int) {
        score.text = swipeCount.toString()
        if (ifBestZero) {
            best.text = score.text
        }
    }

    fun sumEnable(): Boolean {

        for (b in getTiles()) {
            if (b.text.toString().isEmpty())
                return true
        }

        val allDirections = arrayOf(swipeRight(), swipeLeft(), swipeBottom(), swipeUp())
        for (swipe in allDirections) {
            for (row in swipe) {
                if (row.any { b -> b.text.toString().isNotEmpty() }) {
                    if (row[3].text.toString().isNotEmpty()) {
                        if (row[2].text.toString().isNotEmpty() && row[3].text == row[2].text) {
                            return true
                        } else if (row[2].text.toString().isEmpty() && row[1].text.toString().isNotEmpty() && row[3].text == row[1].text) {
                            return true
                        } else if (row[2].text.toString().isEmpty() && row[1].text.toString().isEmpty() && row[0].text.toString().isNotEmpty() && row[3].text == row[0].text) {
                            return true
                        }
                    }
                    if (row[2].text.toString().isNotEmpty()) {
                        if (row[1].text.toString().isNotEmpty() && row[2].text == row[1].text) {
                            return true
                        } else if (row[1].text.toString().isEmpty() && row[0].text.toString().isNotEmpty() && row[2].text == row[0].text) {
                            return true
                        }
                    }
                    if (row[1].text.toString().isNotEmpty()) {
                        if (row[0].text.toString().isNotEmpty() && row[1].text == row[0].text) {
                            return true
                        }
                    }
                }
            }

        }
        return false
    }

    private fun changeTileStyle(tile: Button) {
        if (tile.text.toString().isEmpty()) {
            tile.setBackgroundColor(0xFFCDC1B5.toInt())
        } else {
            when (tile.text.toString().toInt()) {
                2 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t2))
                }
                4 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t4))
                }
                8 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t8))
                }
                16 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t16))

                }
                32 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t32))
                }
                64 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t64))
                }
                128 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t128))
                }
                256 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t256))
                }
                512 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t512))
                }
                1024 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t1024))
                }
                2048 -> {
                    tile.setBackgroundColor(ContextCompat.getColor(this, R.color.t2048))
                    tile.setTextColor(ContextCompat.getColor(this, R.color.yelow))
                    win()
                }
                else -> {
                    err.text = tile.text
                }
            }
        }
    }

    private fun getTilesAndTheirNames(): HashMap<String, Button> {
        val hMap = HashMap<String, Button>()
        var i = 0
        for (b in getTiles()) {
            hMap.put("b$i", b)
            i++
        }
        return hMap
    }

    fun getTiles(): Array<Button> {
        return arrayOf(b0, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15)
    }

    private fun swipeRight(): Array<Array<Button>> {
        return arrayOf(
            arrayOf(b0, b1, b2, b3),
            arrayOf(b4, b5, b6, b7),
            arrayOf(b8, b9, b10, b11),
            arrayOf(b12, b13, b14, b15)
        )
    }

    private fun swipeLeft(): Array<Array<Button>> {
        return arrayOf(
            arrayOf(b3, b2, b1, b0),
            arrayOf(b7, b6, b5, b4),
            arrayOf(b11, b10, b9, b8),
            arrayOf(b15, b14, b13, b12)
        )
    }

    private fun swipeBottom(): Array<Array<Button>> {
        return arrayOf(
            arrayOf(b0, b4, b8, b12),
            arrayOf(b1, b5, b9, b13),
            arrayOf(b2, b6, b10, b14),
            arrayOf(b3, b7, b11, b15)
        )
    }

    private fun swipeUp(): Array<Array<Button>> {
        return arrayOf(
            arrayOf(b12, b8, b4, b0),
            arrayOf(b13, b9, b5, b1),
            arrayOf(b14, b10, b6, b2),
            arrayOf(b15, b11, b7, b3)
        )
    }

    private fun dir(direction: String) = when (direction) {
        "top" -> {
            swipeUp()
        }
        "bottom" -> {
            swipeBottom()
        }
        "right" -> {
            swipeRight()
        }
        "left" -> {
            swipeLeft()
        }
        else -> arrayOf(arrayOf())
    }


}

