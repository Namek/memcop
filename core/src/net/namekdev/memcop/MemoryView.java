package net.namekdev.memcop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.czyzby.lml.annotation.LmlAction;
import com.github.czyzby.lml.annotation.LmlActor;
import com.github.czyzby.lml.parser.impl.AbstractLmlView;
import net.namekdev.memcop.domain.Assembly;
import net.namekdev.memcop.domain.Assembly.AssemblyCompilationError;
import net.namekdev.memcop.domain.Assembly.Instruction;
import net.namekdev.memcop.domain.Level;
import net.namekdev.memcop.domain.Sector;
import net.namekdev.memcop.domain.Transputer;

import java.util.List;

public class MemoryView extends AbstractLmlView {
    @LmlActor("random")
    private Label result;


    public Level level = Level.create1();
    public Transputer transputer;


    public MemoryView() {
        super(newStage());

        transputer = new Transputer();
        transputer.sourceMemory = level;
        transputer.destMemory = level;

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
                    "mov $b $c"
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
        return new Stage(new FitViewport(MemcopGame.WIDTH, MemcopGame.HEIGHT), core.getBatch());
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

    @LmlAction("memoryBlocks")
    public Array<Sector> getMemoryBlocks() {
        return level.memoryBlocks;
    }

    @LmlAction("memoryBlocksPerRow")
    public int memoryBlocksPerRow() { return level.width; }
}