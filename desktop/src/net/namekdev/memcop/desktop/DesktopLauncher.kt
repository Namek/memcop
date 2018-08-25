package net.namekdev.memcop.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import net.namekdev.memcop.MemcopGame

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()
        val pixelPerInch = java.awt.Toolkit.getDefaultToolkit().screenResolution
        val scale = pixelPerInch / 96f
        config.width = (scale * MemcopGame.WIDTH).toInt()
        config.height = (scale * MemcopGame.HEIGHT).toInt()

        LwjglApplication(MemcopGame(), config)
    }
}
