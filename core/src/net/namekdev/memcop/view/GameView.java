package net.namekdev.memcop.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.impl.AbstractLmlView;
import net.namekdev.memcop.MemcopGame;
import net.namekdev.memcop.domain.Assembly;
import net.namekdev.memcop.domain.Assembly.AssemblyCompilationError;
import net.namekdev.memcop.domain.Level;
import net.namekdev.memcop.domain.Sector;
import net.namekdev.memcop.domain.Transputer;

public class GameView extends AbstractLmlView {
    @LmlActor("random") private Label result;

    //@LmlActor("templateInput") private CodeTextArea templateInput;


    public Level level = Level.create2();
    public Transputer transputer;


    public GameView() {
        super(newStage());

        transputer = new Transputer();
        transputer.sourceMemory = level.inputMem;
        transputer.destMemory = level.outputMem;

        level.reset();
        transputer.reset();

        try {
            transputer.instructions = Assembly.compile("" +
                    "mov 5 $a\n" +
                    "mov 0 $b\n" +
                    "_start:\n" +
                    "add $b 2 $b\n" +
                    "cmp $b $a\n" +
                    "jge _start\n" +
                    "sub $b 2 $b\n" + //expecting b = 4
                    "\n" +
                    "_end:\n" +
                    "mov $b $c",

                    level
            );

            while (transputer.forward()) {}
            int c = transputer.registerStates[3].value;
        }
        catch (AssemblyCompilationError err) {
            err.printStackTrace();
        }
    }


    /**
     * @return a new customized {@link Stage} instance.
     */
    public static Stage newStage() {
        MemcopGame core = (MemcopGame) Gdx.app.getApplicationListener();
        Stage stage = new Stage(new FitViewport(MemcopGame.WIDTH, MemcopGame.HEIGHT), core.getBatch());

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
    public FileHandle getTemplateFile() {
        return Gdx.files.internal("views/first.lml");
    }

    @Override
    public String getViewId() {
        return "first";
    }

    @LmlAction("roll")
    public void rollNumber() {
        result.setText(String.valueOf((int) (MathUtils.random() * 1000)));
    }

    @LmlAction("compileCode")
    public void compileCode() {
        // TODO button
    }

    @LmlAction("getInputMemActor")
    public Actor getInputMemActor() {
        return new MemorySourceRenderer(level.inputMem);
    }

    @LmlAction("getOutputMemActor")
    public Actor getOutputMemActor() {
        return new MemorySourceRenderer(level.outputMem);
    }
}