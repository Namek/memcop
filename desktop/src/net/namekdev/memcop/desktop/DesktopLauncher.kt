package net.namekdev.memcop.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import net.namekdev.memcop.MemcopGame
import net.namekdev.memcop.view.Render

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val pixelPerInch = java.awt.Toolkit.getDefaultToolkit().screenResolution
        Render.scale = pixelPerInch / 96f

        val config = LwjglApplicationConfiguration()
        config.width = (Render.width).toInt()
        config.height = (Render.height).toInt()

        LwjglApplication(MemcopGame(), config)
    }
}
