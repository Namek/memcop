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
import net.namekdev.memcop.domain.Level;
import net.namekdev.memcop.domain.Sector;

public class MemoryView extends AbstractLmlView {
    @LmlActor("random")
    private Label result;


    public Level level = Level.create1();


    public MemoryView() {
        super(newStage());
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

    @LmlAction("memoryBlocks")
    public Array<Sector> getMemoryBlocks() {
        return level.memoryBlocks;
    }

    @LmlAction("memoryBlocksPerRow")
    public int memoryBlocksPerRow() { return level.width; }
}