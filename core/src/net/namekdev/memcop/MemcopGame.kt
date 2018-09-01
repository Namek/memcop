package net.namekdev.memcop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.czyzby.kiwi.util.gdx.asset.Disposables
import com.github.czyzby.lml.parser.LmlParser
import com.github.czyzby.lml.parser.impl.DefaultLmlData
import com.github.czyzby.lml.util.LmlApplicationListener
import com.github.czyzby.lml.vis.util.VisLml
import net.namekdev.memcop.view.GameView
import net.namekdev.memcop.view.LevelListView
import net.namekdev.memcop.view.Render

class MemcopGame : LmlApplicationListener() {
    private lateinit var batch: Batch


    override fun create() {
        Assets.load()
        super.create()

        batch = SpriteBatch()
        batch.enableBlending()

        val stage = newStage(batch)
        val view = GameView(stage)
        initiateView(view)
        setView(view)


        //saveDtdSchema(Gdx.files.local("lml.dtd"));
    }


    override fun dispose() {
        super.dispose()
        Disposables.disposeOf(batch)
        Assets.disposeAll()
    }

    override fun createParser(): LmlParser {
        val data = DefaultLmlData()
        data.defaultSkin = Assets.skin
        data.defaultI18nBundle = I18NBundle.createBundle(Gdx.files.internal("i18n/bundle"))

        return VisLml.parser(data).build()
    }

    /**
     * @return a new customized [Stage] instance.
     * @param batch
     */
    fun newStage(batch: Batch): Stage {
        val stage = Stage(FitViewport(Render.width, Render.height), batch)

        stage.addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if (keycode == Input.Keys.ESCAPE) {
                    if (currentView is GameView)
                        Gdx.app.exit()
                    else {
                        clearViews()
                        val view = GameView(newStage(batch))
                        initiateView(view)
                        setView(view)
                    }
                }
                else if (keycode == Input.Keys.F10) {
                    clearViews()
                    val view = LevelListView(newStage(batch))
                    initiateView(view)
                    setView(view)
                }

                return super.keyDown(event, keycode)
            }
        })
        return stage
    }
}
