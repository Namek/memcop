package net.namekdev.memcop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.czyzby.kiwi.util.gdx.asset.Disposables;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.util.LmlApplicationListener;
import com.github.czyzby.lml.vis.util.VisLml;
import com.kotcrab.vis.ui.VisUI;
import net.namekdev.memcop.view.GameView;

public class MemcopGame extends LmlApplicationListener {
    public static final int WIDTH = 900, HEIGHT = 600;
    private Batch batch;


    @Override
    public void create() {
        super.create();
        batch = new SpriteBatch();
        batch.enableBlending();

        Assets.load();

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
        Assets.disposeAll();
    }

    @Override
    protected LmlParser createParser() {
        return VisLml.parser()
                .i18nBundle(I18NBundle.createBundle(Gdx.files.internal("i18n/bundle")))
                .build();
    }
}
