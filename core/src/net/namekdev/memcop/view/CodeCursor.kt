package net.namekdev.memcop.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.kotcrab.vis.ui.widget.HighlightTextArea
import net.namekdev.memcop.Assets
import net.namekdev.memcop.domain.Assembly
import net.namekdev.memcop.domain.Transputer

class CodeCursor(private val codeInput: HighlightTextArea, private val transputer: Transputer) : Actor() {

    init {
        touchable = Touchable.disabled
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        val instr = transputer.nextInstruction
        if (!transputer.isDebuggingActive)
            return

        val s = Render.scale
        val lineHeight = codeInput.style.font.lineHeight
        val y: Float
        val h: Float

        if (instr != null) {
            y = codeInput.height - instr.lineNumber * lineHeight + 4 * s
            h = lineHeight - 2 * s
        }
        else {
            // render bottom line when whole program is finished
            y = codeInput.height - transputer.lastInstruction!!.lineNumber * lineHeight + 4 * s
            h = 4f * s
        }

        batch.color = COLOR_CURSOR
        batch.draw(Assets.white, 0f, y, codeInput.width, h)
    }

    companion object {
        private val COLOR_CURSOR = Color.valueOf("790000")
    }
}
