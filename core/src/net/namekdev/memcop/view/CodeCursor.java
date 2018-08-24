package net.namekdev.memcop.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.kotcrab.vis.ui.widget.HighlightTextArea;
import net.namekdev.memcop.Assets;
import net.namekdev.memcop.domain.Assembly;
import net.namekdev.memcop.domain.Transputer;

public class CodeCursor extends Actor {
    private static final Color COLOR_CURSOR = Color.valueOf("790000");

    private HighlightTextArea codeInput;
    private Transputer transputer;

    public CodeCursor(HighlightTextArea codeInput, Transputer transputer) {
        this.codeInput = codeInput;
        this.transputer = transputer;

        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        final Assembly.Instruction instr = transputer.getNextInstruction();
        if (!transputer.isDebuggingActive())
            return;

        float lineHeight = codeInput.getStyle().font.getLineHeight();
        float y, h;

        if (instr != null) {
            y = codeInput.getHeight() - instr.lineNumber * lineHeight + 4;
            h = lineHeight - 2;
        }
        else {
            // render bottom line when whole program is finished
            y = codeInput.getHeight() - transputer.lastInstruction.lineNumber * lineHeight + 4;
            h = 4;
        }

        batch.setColor(COLOR_CURSOR);
        batch.draw(Assets.white, 0, y, codeInput.getWidth(), h);
    }
}
