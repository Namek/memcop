package net.namekdev.memcop.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import net.namekdev.memcop.MemcopGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = MemcopGame.WIDTH;
		config.height = MemcopGame.HEIGHT;
		new LwjglApplication(new MemcopGame(), config);
	}
}
