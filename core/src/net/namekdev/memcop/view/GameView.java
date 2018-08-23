package net.namekdev.memcop.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.github.czyzby.lml.annotation.*;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.AbstractLmlView;
import com.kotcrab.vis.ui.widget.HighlightTextArea;
import net.namekdev.memcop.domain.Assembly;
import net.namekdev.memcop.domain.Assembly.AssemblyCompilationError;
import net.namekdev.memcop.domain.Level;
import net.namekdev.memcop.domain.LevelFactory;
import net.namekdev.memcop.domain.Transputer;
import net.namekdev.memcop.view.widgets.LmlSourceHighlighter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GameView extends AbstractLmlView {
    private LmlParser parser;
    private Stage stage;

    @LmlActor("btnRun") public TextButton btnRun;
    @LmlActor("btnDebug") private TextButton btnDebug;
    @LmlActor("btnStop") private TextButton btnStop;
    @LmlActor("btnShowGoal") private TextButton btnShowGoal;

    private final HighlightTextArea codeInput = new HighlightTextArea("");


    Level level = LevelFactory.create(4);
    Transputer transputer;
    Map<String, Label> registerValueLabels = new TreeMap<String, Label>();


    public GameView(Stage stage) {
        super(stage);
        this.stage = stage;

        transputer = new Transputer();
        transputer.sourceMemory = level.inputMem;
        transputer.destMemory = level.outputMem;
        transputer.reset();

        codeInput.setHighlighter(new LmlSourceHighlighter());
        codeInput.setMessageText("If you need help... hit F1");
        codeInput.setFocusBorderEnabled(false);
        codeInput.getColor().a = 0.6f;
    }

    @LmlBefore
    public void before(final LmlParser parser) {
        this.parser = parser;
    }

    @LmlAfter
    public void afterParse() {
        btnShowGoal.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                MemorySourceRenderer.drawGoal = true;
                super.enter(event, x, y, pointer, fromActor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                MemorySourceRenderer.drawGoal = false;
                super.exit(event, x, y, pointer, toActor);
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                MemorySourceRenderer.drawGoal = true;
                return super.mouseMoved(event, x, y);
            }
        });
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

    @Override
    public FileHandle getTemplateFile() {
        return Gdx.files.internal("views/first.lml");
    }

    @Override
    public String getViewId() {
        return "first";
    }

    @LmlAction("codeInput")
    public HighlightTextArea getCodeInput() {
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

    @LmlAction("createRegisterValueLabel")
    public Actor createRegisterValueLabel(String name) {
        Skin skin = parser.getData().getDefaultSkin();
        Label label = new Label("0", skin);
        registerValueLabels.put("$" + name.replace("reg_", ""), label);

        return label;
    }

    @LmlAction("goalDescription")
    public String getGoalDescription() {
        return level.getGoalDescription();
    }

    @LmlAction("registers")
    public List<String> getRegisterList() {
        List<String> names = new ArrayList<String>();

        for (Transputer.RegisterState reg : transputer.registerStates) {
            names.add(reg.info.name.replace("$", ""));
        }
        return names;
    }


    /**
     * Simulates whole program.
     * If program is in the middle and no code was changed then continue without reset.
     */
    @LmlAction("run")
    public void run() {
        if (transputer.lastInstruction == null) {
            reset();
            compileCode();
        }

        while (transputer.step()) {
            updateRegisters();
        }
    }

    /**
     * Start debugging by going step by step.
     */
    @LmlAction("debug")
    public void debug() {
        transputer.step();
        updateRegisters();
    }

    @LmlAction("stop")
    public void reset() {
        transputer.reset();
        level.reset();
        updateButtonTexts();
        updateRegisters();
    }

    private void compileCode() {
        try {
            final String code = codeInput.getText();
            transputer.instructions = Assembly.compile(code, level);
        }
        catch (AssemblyCompilationError assemblyCompilationError) {
            assemblyCompilationError.printStackTrace();
        }
    }


    private void updateButtonTexts() {
        btnRun.setText(transputer.lastInstruction == null ? "Run" : "Continue");
        btnDebug.setText(transputer.lastInstruction == null ? "Debug" : "Step");
    }

    private void updateRegisters() {
        for (Transputer.RegisterState reg : transputer.registerStates) {
            Label valueLabel = registerValueLabels.get(reg.info.name);
            valueLabel.setText(String.valueOf(reg.value));
        }
    }
}