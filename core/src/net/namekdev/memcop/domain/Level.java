package net.namekdev.memcop.domain;

import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays;

@SuppressWarnings("ALL")
public class Level {
    public static final char EMPTY_SECTOR = ' ';
    public static final char BAD_SECTOR = '✕';


    public MemorySource inputMem;
    public MemorySource outputMem;

    public Level(MemorySource inputMem, MemorySource outputMem) {
        this.inputMem = inputMem;
        this.outputMem = outputMem;
    }

    public void reset() {
        inputMem.reset();
        outputMem.reset();
    }


    public static Level create1() {
        return null;
    }

    public static Level create2() {
        int w = 12, h = 15, outputSize = 12 * 15, inputSize = 48;
        Array<Sector> sectors = GdxArrays.newArray(outputSize);
        sectors.setSize(outputSize);
        for (int i = 0; i < outputSize; ++i)
            sectors.set(i, new Sector());

        int y = 0;
        boolean padLeft = false;
        int brokenSectors = 0;

        do {
            int startX = 1;
            y += 1;

            if (padLeft)
                startX += 1;

            int i = y * w + startX;
            if (i >= outputSize) break;
            sectors.set(i, Sector.newBroken());
            brokenSectors += 1;

            i += 3;
            if (i >= outputSize) break;
            sectors.set(i, Sector.newBroken());
            brokenSectors += 1;

            i += 5;
            if (i >= outputSize) break;
            sectors.set(i, Sector.newBroken());
            brokenSectors += 1;

            y += 1;
            padLeft = !padLeft;
        }
        while (y < h);

        MemorySource inputMem = new MemorySource(w, inputSize);
        inputMem.canReadFrom = true;

        MemorySource outputMem = new MemorySource(w, sectors);
        outputMem.canWriteTo = true;
        outputMem.canWriteToSpecificIndex = true;

        return new Level(inputMem, outputMem);
    }

}
