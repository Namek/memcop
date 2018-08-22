package net.namekdev.memcop.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import net.namekdev.memcop.MemcopGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		int pixelPerInch = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
		float scale = pixelPerInch / 96f;
		config.width = (int) (scale * MemcopGame.WIDTH);
		config.height = (int) (scale * MemcopGame.HEIGHT);
		new LwjglApplication(new MemcopGame(), config);
	}
}
