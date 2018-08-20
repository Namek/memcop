package net.namekdev.memcop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.I18NBundle;
import com.github.czyzby.kiwi.util.gdx.asset.Disposables;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.util.LmlApplicationListener;
import com.github.czyzby.lml.vis.util.VisLml;
import com.kotcrab.vis.ui.VisUI;

public class MemcopGame extends LmlApplicationListener {
    public static final int WIDTH = 900, HEIGHT = 600;
    private SpriteBatch batch;


    @Override
    public void create() {
        super.create();
        batch = new SpriteBatch();

        // dpi
        float s = 1;// Gdx.graphics.getPpiX() / 96f;
        int w = (int)(WIDTH * s);
        int h = (int)(HEIGHT * s);
        Gdx.graphics.setWindowedMode(w, h);

        setView(GameView.class);


        //saveDtdSchema(Gdx.files.local("lml.dtd"));
    }

    /**
     * @return application's only {@link Batch}.
     */
    public Batch getBatch() {
        return batch;
    }

    @Override
    public void dispose() {
        super.dispose();
        Disposables.disposeOf(batch);
        VisUI.dispose();
    }

    @Override
    protected LmlParser createParser() {
        return VisLml.parser()
                .i18nBundle(I18NBundle.createBundle(Gdx.files.internal("i18n/bundle")))
                .build();
    }
}
