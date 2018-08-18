package net.namekdev.memcop.domain;

import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays;

@SuppressWarnings("ALL")
public class Level implements IInputMemory, IOutputMemory {
    public static final char EMPTY_SECTOR = ' ';
    public static final char BAD_SECTOR = '✕';


    public final int inputBytesCount;
    public final int width;
    public final Array<Sector> memoryBlocks;

    private int inputCursor = 0;


    public Level(int inputBytesCount, int memoryViewWidth, Array<Sector> memoryBlocks) {
        this.inputBytesCount = inputBytesCount;
        this.width = memoryViewWidth;
        this.memoryBlocks = memoryBlocks;
    }

    public void reset() {
        inputCursor = 0;
        for (Sector sector : memoryBlocks)
            sector.reset();
    }

    public byte readSourceByte() {
        inputCursor += 1;

        // for now, let's just return this every time as we don't visualise the actual value anyway
        return 127;
    }

    public boolean saveByte(int destinationIndex, byte value) {
        Sector sector = memoryBlocks.get(destinationIndex);

        if (sector == null)
            return false;

        sector.write(value);
        return true;
    }



    public static Level create1() {
        int w = 12, h = 15, size = 12 * 15;
        Array<Sector> blocks = GdxArrays.newArray(size);
        blocks.setSize(size);
        for (int i = 0; i < size; ++i)
            blocks.set(i, new Sector());

        int y = 0;
        boolean padLeft = false;
        int brokenBlocks = 0;

        do {
            int startX = 1;
            y += 1;

            if (padLeft)
                startX += 1;

            int i = y * w + startX;
            if (i >= size) break;
            blocks.set(i, Sector.newBroken());
            brokenBlocks += 1;

            i += 3;
            if (i >= size) break;
            blocks.set(i, Sector.newBroken());
            brokenBlocks += 1;

            i += 5;
            if (i >= size) break;
            blocks.set(i, Sector.newBroken());
            brokenBlocks += 1;

            y += 1;
            padLeft = !padLeft;
        }
        while (y < h);

        return new Level(size - brokenBlocks, w, blocks);
    }

}
