package net.namekdev.memcop.view;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import net.namekdev.memcop.Assets;
import net.namekdev.memcop.domain.MemorySource;

public class MemorySourceRenderer extends Actor {
    public MemorySource memSource;
    public static final int PADDING = 2;
    public static final int CELL_SIZE = 22;

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

        float y = getY() + getHeight() - CELL_SIZE;
        for (int iy = 0; iy < ih; ++iy) {
            float x = getX();
            for (int ix = 0; ix < iw; ++ix) {
                batch.draw(Assets.gray, x, y, CELL_SIZE, CELL_SIZE);
                x += CELL_SIZE + PADDING;
            }
            y -= CELL_SIZE + PADDING;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
