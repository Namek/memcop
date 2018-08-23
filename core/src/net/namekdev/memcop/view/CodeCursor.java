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

        final Assembly.Instruction instr = transputer.lastInstruction;
        if (instr == null)
            return;

        Assembly.Instruction lastInstr = transputer.instructions.get(transputer.instructions.size() - 1);
        float lineHeight = codeInput.getStyle().font.getLineHeight();
        float y = codeInput.getHeight() - (instr.lineNumber + 1) * lineHeight + 4;

        batch.setColor(COLOR_CURSOR);
        batch.draw(Assets.white, 0, y, codeInput.getWidth(), lineHeight - 2);
    }
}
