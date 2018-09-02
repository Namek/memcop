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

    lateinit var levelConstructors: List<() -> Level>

    init {
        loadLevels()

        // a runtime check to see if all levels at least construct properly
        levelConstructors.forEach { it.invoke() }
    }

    fun loadLevels() {
        val code = "(function() { " + Gdx.files.internal("levels.js").readString() + " ; })();"
        val levelJsons = jsEngine.eval(code).toString()
        val levelInfos = adapter.fromJson(levelJsons)!!

        levelConstructors = levelInfos
            .map { l ->
                { ->
                    val memories = l.memories.map { MemorySource.fromPojo(it) }
                    val validators = l.validators.map { LevelCompletionValidator.fromPojo(it) }

                    Level(l.name, l.goalDescription, memories, validators, l.shouldCopyCodeSolutionFromPreviousLevel)
                }
            }
    }

    fun create(index: Int): Level = levelConstructors.get(index).invoke()
}
