package net.namekdev.memcop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.github.czyzby.kiwi.util.gdx.asset.Disposables;
import com.github.czyzby.lml.parser.LmlData;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.DefaultLmlData;
import com.github.czyzby.lml.util.LmlApplicationListener;
import com.github.czyzby.lml.vis.util.VisLml;
import com.kotcrab.vis.ui.VisUI;
import net.namekdev.memcop.view.GameView;

public class MemcopGame extends LmlApplicationListener {
    public static final int WIDTH = 900, HEIGHT = 600;

    private Batch batch;


    @Override
    public void create() {
        Assets.load();
        super.create();

        batch = new SpriteBatch();
        batch.enableBlending();

        Stage stage = newStage(batch);
        GameView view = new GameView(stage);
        initiateView(view);
        setView(view);


        //saveDtdSchema(Gdx.files.local("lml.dtd"));
    }

    /**
     * @return a new customized {@link Stage} instance.
     * @param batch
     */
    public static Stage newStage(Batch batch) {
        Stage stage = new Stage(new ScalingViewport(Scaling.fit, MemcopGame.WIDTH, MemcopGame.HEIGHT), batch);

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE)
                    Gdx.app.exit();

                return super.keyDown(event, keycode);
            }
        });
        return stage;
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
        LmlData data = new DefaultLmlData();
        data.setDefaultSkin(Assets.skin);
        data.setDefaultI18nBundle(I18NBundle.createBundle(Gdx.files.internal("i18n/bundle")));

        return VisLml.parser(data).build();
    }
}
