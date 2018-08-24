package net.namekdev.memcop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;

import java.util.ArrayList;
import java.util.List;

public abstract class Assets {
    public static AssetManager assetManager = new AssetManager();
    public static TextureRegion white;
    public static Skin skin;

    public static final List<Disposable> disposables = new ArrayList<Disposable>();

    public static void load() {
        white = genColorTex(Color.WHITE);

        String skinPath = "skin/x2/uiskin.json";
        assetManager.load(skinPath, Skin.class);

        assetManager.finishLoading();
        skin = assetManager.get(skinPath);

        VisUI.load(skin);
    }

    public static TextureRegion genColorTex(Color color) {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.drawPixel(0, 0, Color.rgba8888(color));
        TextureRegion colTex = new TextureRegion(new Texture(pix));
        disposables.add(pix);

        return colTex;
    }

    public static void disposeAll() {
        for (Disposable d : disposables)
            d.dispose();
        assetManager.dispose();
    }

}
