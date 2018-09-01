package net.namekdev.memcop.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.czyzby.lml.annotation.LmlAction
import com.github.czyzby.lml.parser.LmlParser

class LevelListView(_stage: Stage) : AbstractView("level_list", "level_list", _stage) {
    private var parser: LmlParser? = null

    private var levelsDone = 0
    lateinit private var currentTitle: String


    @LmlAction("currentTitle")
    fun getCurrentTitle(): String {
        return currentTitle
    }


    init {
        levelsDone = 100

        if (levelsDone < 4)
            currentTitle = "Beginning..."
    }



}