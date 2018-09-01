package net.namekdev.memcop.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.czyzby.lml.annotation.LmlAction
import com.github.czyzby.lml.parser.impl.AbstractLmlView

abstract class AbstractView(
    val _viewId: String,
    val templateFileName: String,
    _stage: Stage
) : AbstractLmlView(_stage) {
    @get:LmlAction("scale") val scale = Render.scale

    override fun getTemplateFile(): FileHandle {
        return Gdx.files.internal("views/$templateFileName.lml")
    }

    override fun getViewId(): String {
        return _viewId
    }
}