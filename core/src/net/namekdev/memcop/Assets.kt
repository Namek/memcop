package net.namekdev.memcop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.SkinLoader
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ObjectMap
import com.kotcrab.vis.ui.VisUI
import net.namekdev.memcop.view.Render
import java.util.*
import kotlin.math.roundToInt

object Assets {
    var assetManager = AssetManager()
    lateinit var white: TextureRegion
    lateinit var skin: Skin

    val disposables: MutableList<Disposable> = ArrayList()

    fun load() {
        white = genColorTex(Color.WHITE)

        val textFontGen = FreeTypeFontGenerator(Gdx.files.internal("fonts/Cinzel-Regular.ttf"))
        val codeFontGen = FreeTypeFontGenerator(Gdx.files.internal("fonts/IBMPlexMono-Regular.ttf"))
        val fontParams = FreeTypeFontGenerator.FreeTypeFontParameter()
        fontParams.size = (18 * Render.scale).roundToInt()
        val headerFont = textFontGen.generateFont(fontParams)
        fontParams.size = (14 * Render.scale).roundToInt()
        val defaultFont = textFontGen.generateFont(fontParams)
        fontParams.size = (12 * Render.scale).roundToInt()
        val smallFont = textFontGen.generateFont(fontParams)
        fontParams.size = (16 * Render.scale).roundToInt()
        val codeFont = codeFontGen.generateFont(fontParams)

        textFontGen.dispose()
        codeFontGen.dispose()

        val skinPath = "skin/uiskin.json"
        val skinResources = ObjectMap<String, Any>()
        skinResources.put("header-font", headerFont)
        skinResources.put("default-font", defaultFont)
        skinResources.put("small-font", smallFont)
        skinResources.put("code-font", codeFont)
        val skinParam = SkinLoader.SkinParameter(skinResources)
        assetManager.load(skinPath, Skin::class.java, skinParam)
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
        VisUI.dispose(false)
    }

}
