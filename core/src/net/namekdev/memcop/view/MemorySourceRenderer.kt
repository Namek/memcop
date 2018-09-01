package net.namekdev.memcop.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import net.namekdev.memcop.Assets
import net.namekdev.memcop.domain.MemorySource
import kotlin.math.roundToInt

class MemorySourceRenderer(var memSource: MemorySource) : Actor() {
    init {
        width = (memSource.sectorsPerRow * (CELL_SIZE + PADDING) - PADDING)
        height = (memSource.totalHeight * (CELL_SIZE + PADDING) - PADDING)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        val iw = memSource.sectorsPerRow
        val ih = memSource.totalHeight
        val lastTouchedIndex = memSource.lastPos
        val tix = lastTouchedIndex % iw
        val tiy = lastTouchedIndex / iw

        var y = y + height - CELL_SIZE
        var iy = 0
        var i = 0
        while (iy < ih) {
            var x = x
            var ix = 0
            while (ix < iw && i < memSource.sectors.size) {
                val sector = memSource.sectors.get(i)
                var cellBg = COLOR_UNTOUCHED
                var cursorColor = COLOR_CURSOR
                var renderCursor = ix == tix && iy == tiy

                if (sector.written)
                    cellBg = COLOR_WRITTEN

                if (sector.broken) {
                    cellBg = COLOR_BROKEN
                }
                else if (drawGoal && sector.markedForGradient) {
                    cellBg = color
                    cellBg.set(COLOR_FILLED_OK_MIN).lerp(COLOR_FILLED_OK_MAX, sector.levelInputGradient)

                    if (sector.written && !sector.broken) {
                        if (!renderCursor)
                            cursorColor = COLOR_WRITTEN

                        renderCursor = true
                    }
                }

                batch.color = cellBg
                batch.draw(Assets.white, x, y, CELL_SIZE, CELL_SIZE)

                if (renderCursor) {
                    batch.color = cursorColor
                    batch.draw(Assets.white, x, y, CELL_SIZE, (CELL_SIZE / 5))
                }

                x += (CELL_SIZE + PADDING)
                ++ix
                ++i
            }
            y -= (CELL_SIZE + PADDING)
            ++iy
        }

        batch.color = Color.WHITE
    }


    companion object {
        val PADDING = (2 * Render.scale).roundToInt()
        val CELL_SIZE = 20 * Render.scale
        internal val COLOR_WRITTEN = Color(0.137f, 0.607f, 0.262f, 1f)
        internal val COLOR_UNTOUCHED = Color.DARK_GRAY
        internal val COLOR_BROKEN = Color.valueOf("9b311d")
        internal val COLOR_FILLED_OK_MIN = Color.valueOf("443aad")
        internal val COLOR_FILLED_OK_MAX = Color.valueOf("0c0463")
        internal val COLOR_CURSOR = Color.valueOf("eeeeee")

        var drawGoal = false
    }
}
