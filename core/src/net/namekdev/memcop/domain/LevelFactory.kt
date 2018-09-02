package net.namekdev.memcop.domain

import com.badlogic.gdx.Gdx
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import net.namekdev.memcop.domain.pojo.LevelInfo
import javax.script.ScriptEngineManager

object LevelFactory {
    val moshi = Moshi.Builder().build()
    val adapter = moshi.adapter<List<LevelInfo>>(Types.newParameterizedType(List::class.java, LevelInfo::class.java))
    val jsEngine = ScriptEngineManager().getEngineByName("nashorn")

    var _levelDefinitions: kotlin.Array<() -> Level> = arrayOf()

    init {
        initLevels()
    }

    fun initLevels() {
        val levelJsons = jsEngine.eval(Gdx.files.internal("levels.js").readString()).toString()
        val levelInfos = adapter.fromJson(levelJsons)!!

        _levelDefinitions = levelInfos
            .map { l ->
                val memories = l.memories.map { MemorySource.fromPojo(it) }
                val validators = l.validators.map { LevelCompletionValidator.fromPojo(it) }

                Level(l.name, l.goalDescription, memories, validators, l.shouldCopyCodeSolutionFromPreviousLevel)
            }
            .map { l -> { -> l} }
            .toTypedArray()
    }

    fun create(index: Int): Level {
        val l = _levelDefinitions.get(index).invoke()
        l.reset()
        return l
    }
}
