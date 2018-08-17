package net.namekdev.memcop.domain;

import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays;

@SuppressWarnings("ALL")
public class Level {
    public static final char EMPTY_SECTOR = ' ';
    public static final char BAD_SECTOR = '✕';


    public final int width;
    public final Array<Sector> memoryBlocks;


    public Level(int width, Array<Sector> memoryBlocks) {
        this.width = width;
        this.memoryBlocks = memoryBlocks;
    }

    public static Level create1() {
        int w = 12, h = 15, size = 12 * 15;
        Array<Sector> blocks = GdxArrays.newArray(size);
        blocks.setSize(size);
        for (int i = 0; i < size; ++i)
            blocks.set(i, new Sector());

        int y = 0;
        boolean padLeft = false;

        do {
            int startX = 1;
            y += 1;

            if (padLeft)
                startX += 1;

            int i = y * w + startX;
            if (i >= size) break;
            blocks.set(i, Sector.newBroken());
            i += 3;
            if (i >= size) break;
            blocks.set(i, Sector.newBroken());
            i += 5;
            if (i >= size) break;
            blocks.set(i, Sector.newBroken());

            y += 1;
            padLeft = !padLeft;
        }
        while (y < h);

        return new Level(w, blocks);
    }

}
