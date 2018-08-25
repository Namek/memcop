package net.namekdev.memcop

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Disposable
import com.kotcrab.vis.ui.VisUI

import java.util.ArrayList

object Assets {
    var assetManager = AssetManager()
    lateinit var white: TextureRegion
    lateinit var skin: Skin

    val disposables: MutableList<Disposable> = ArrayList()

    fun load() {
        white = genColorTex(Color.WHITE)

        val skinPath = "skin/x2/uiskin.json"
        assetManager.load(skinPath, Skin::class.java)

        assetManager.finishLoading()
        skin = assetManager.get(skinPath)

        VisUI.load(skin)
    }

    fun genColorTex(color: Color): TextureRegion {
        val pix = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pix.drawPixel(0, 0, Color.rgba8888(color))
        val colTex = TextureRegion(Texture(pix))
        disposables.add(pix)

        return colTex
    }

    fun disposeAll() {
        for (d in disposables)
            d.dispose()
        assetManager.dispose()
    }

}
