package net.namekdev.memcop.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import net.namekdev.memcop.Assets;
import net.namekdev.memcop.domain.MemorySource;
import net.namekdev.memcop.domain.Sector;

public class MemorySourceRenderer extends Actor {
    public MemorySource memSource;
    public static final int PADDING = 2;
    public static final int CELL_SIZE = 22;
    static final Color COLOR_WRITTEN = new Color(0.137f, 0.607f, 0.262f, 1f);
    static final Color COLOR_UNTOUCHED = Color.DARK_GRAY;
    static final Color COLOR_BROKEN = Color.valueOf("9b311d");
    static final Color COLOR_FILLED_OK_MIN = Color.valueOf("443aad");
    static final Color COLOR_FILLED_OK_MAX = Color.valueOf("0c0463");
    static final Color COLOR_CURSOR = Color.valueOf("eeeeee");

    public static boolean drawGoal = true;


    public MemorySourceRenderer(MemorySource memSource) {
        this.memSource = memSource;

        setWidth(memSource.sectorsPerRow * (CELL_SIZE + PADDING) + PADDING);
        setHeight(memSource.getTotalHeight() * (CELL_SIZE + PADDING) + PADDING);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        final int iw = memSource.sectorsPerRow;
        final int ih = memSource.getTotalHeight();
        final int lastTouchedIndex = memSource.getLastPos();
        final int tix = lastTouchedIndex % iw;
        final int tiy = lastTouchedIndex / iw;

        float y = getY() + getHeight() - CELL_SIZE;
        for (int iy = 0, i = 0; iy < ih; ++iy) {
            float x = getX();
            for (int ix = 0; ix < iw && i < memSource.sectors.size; ++ix, ++i) {
                Sector sector = memSource.sectors.get(i);
                Color cellBg = COLOR_UNTOUCHED;
                Color cursorColor = COLOR_CURSOR;
                boolean renderCursor = ix == tix && iy == tiy;

                if (sector.written)
                    cellBg = COLOR_WRITTEN;

                if (sector.broken) {
                    cellBg = COLOR_BROKEN;
                }
                else if (drawGoal && sector.markedForGradient) {
                    cellBg = getColor();
                    cellBg.set(COLOR_FILLED_OK_MIN).lerp(COLOR_FILLED_OK_MAX, sector.levelInputGradient);

                    if (sector.written && !sector.broken) {
                        if (!renderCursor)
                            cursorColor = COLOR_WRITTEN;

                        renderCursor = true;
                    }
                }

                batch.setColor(cellBg);
                batch.draw(Assets.white, x, y, CELL_SIZE, CELL_SIZE);

                if (renderCursor) {
                    batch.setColor(cursorColor);
                    batch.draw(Assets.white, x, y, CELL_SIZE, CELL_SIZE/5);
                }

                x += CELL_SIZE + PADDING;
            }
            y -= CELL_SIZE + PADDING;
        }

        batch.setColor(Color.WHITE);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
