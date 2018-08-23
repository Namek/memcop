package net.namekdev.memcop.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.impl.AbstractLmlView;
import com.kotcrab.vis.ui.widget.HighlightTextArea;
import net.namekdev.memcop.MemcopGame;
import net.namekdev.memcop.domain.Assembly;
import net.namekdev.memcop.domain.Assembly.AssemblyCompilationError;
import net.namekdev.memcop.domain.Level;
import net.namekdev.memcop.domain.Transputer;
import net.namekdev.memcop.view.widgets.LmlSourceHighlighter;

public class GameView extends AbstractLmlView {
    @LmlActor("random") private Label result;

    private final HighlightTextArea codeInput = new HighlightTextArea("");


    public Level level = Level.create(4);
    public Transputer transputer;


    public GameView() {
        super(newStage());

        transputer = new Transputer();
        transputer.sourceMemory = level.inputMem;
        transputer.destMemory = level.outputMem;
        transputer.reset();
    }

    @Override
    public void show() {
        String code = "" +
            "mov 5 $a\n" +
            "mov 0 $b\n" +
            "_start:\n" +
            "ldi $c\n" +
            "sti $c $b\n" +
            "inc $b\n" +
            "cmp $b $a\n" +
            "jge _start\n" +
            "sub $b 2 $b\n" + //expecting b = 4
            "\n" +
            "_end:\n" +
            "mov $b $c";

        codeInput.setText(code);

        compileCode();
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

    @LmlAction("codeInput")
    public HighlightTextArea createCodeInput() {
        codeInput.setHighlighter(new LmlSourceHighlighter());
        codeInput.setMessageText("If you need help... hit F1");
        codeInput.setFocusBorderEnabled(false);
        codeInput.getColor().a = 0.6f;
        return codeInput;
    }

    @LmlAction("codeCursor")
    public Actor createCodeCursor() {
        return new CodeCursor(codeInput, transputer);
    }

    @LmlAction("inputMemActor")
    public Actor createInputMemActor() {
        return new MemorySourceRenderer(level.inputMem);
    }

    @LmlAction("outputMemActor")
    public Actor createOutputMemActor() {
        return new MemorySourceRenderer(level.outputMem);
    }


    @LmlAction("compileCode")
    public void compileCode() {
        try {
            final String code = codeInput.getText();
            transputer.instructions = Assembly.compile(code, level);
        }
        catch (AssemblyCompilationError assemblyCompilationError) {
            assemblyCompilationError.printStackTrace();
        }
    }

    @LmlAction("run")
    public void run() {
        try {
            final String code = codeInput.getText();
            transputer.instructions = Assembly.compile(code, level);

            //noinspection StatementWithEmptyBody
            while (transputer.step()) { }
        }
        catch (AssemblyCompilationError assemblyCompilationError) {
            assemblyCompilationError.printStackTrace();
        }

    }

    @LmlAction("debug")
    public void debug() {
        transputer.step();
    }

    @LmlAction("reset")
    public void reset() {
        transputer.reset();
        level.reset();
    }

}