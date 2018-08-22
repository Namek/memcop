package net.namekdev.memcop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

public abstract class Assets {
    public static TextureRegion white;

    public static final List<Disposable> disposables = new ArrayList<Disposable>();

    public static void load() {
        white = genColorTex(Color.WHITE);
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
    }

}
