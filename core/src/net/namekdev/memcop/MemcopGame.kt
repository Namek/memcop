package net.namekdev.memcop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.github.czyzby.kiwi.util.gdx.asset.Disposables
import com.github.czyzby.lml.parser.LmlData
import com.github.czyzby.lml.parser.LmlParser
import com.github.czyzby.lml.parser.impl.DefaultLmlData
import com.github.czyzby.lml.util.LmlApplicationListener
import com.github.czyzby.lml.vis.util.VisLml
import com.kotcrab.vis.ui.VisUI
import net.namekdev.memcop.view.GameView
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

    companion object {
        /**
         * @return a new customized [Stage] instance.
         * @param batch
         */
        fun newStage(batch: Batch): Stage {
            val stage = Stage(FitViewport(Render.width, Render.height), batch)

            stage.addListener(object : InputListener() {
                override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                    if (keycode == Input.Keys.ESCAPE)
                        Gdx.app.exit()

                    return super.keyDown(event, keycode)
                }
            })
            return stage
        }
    }
}
