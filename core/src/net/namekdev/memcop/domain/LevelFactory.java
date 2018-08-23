package net.namekdev.memcop.domain;

import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays;

public class LevelFactory {

    public static Level create(int index) {
        Level l = null;
        switch (index) {
            case 0: l = create0(); break;
            case 1: l = create1(); break;
            case 2: l = create2(); break;
            case 3: l = create3(); break;
            case 4: l = create4(); break;
            default: throw new Error("unknown level number: " + index);
        }
        l.reset();

        return l;
    }


    // there are no broken sectors, copy the memory once
    public static Level create0() {
        int w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        MemorySource inputMem = new MemorySource(w, inputSize);
        inputMem.canReadFrom = true;

        MemorySource outputMem = new MemorySource(w, outputSize);
        outputMem.canWriteTo = true;
        outputMem.canWriteToSpecificIndex = true;

        return new Level("Basics", inputMem, outputMem, 1);
    }


    // there are no broken sectors, copy same memory 3 times
    public static Level create1() {
        int w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        MemorySource inputMem = new MemorySource(w, inputSize);
        inputMem.canReadFrom = true;

        MemorySource outputMem = new MemorySource(w, outputSize);
        outputMem.canWriteTo = true;
        outputMem.canWriteToSpecificIndex = true;

        return new Level("3 x Basics", inputMem, outputMem, 3);
    }

    // there is exactly one broken sector
    public static Level create2() {
        int w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        MemorySource inputMem = new MemorySource(w, inputSize);
        inputMem.canReadFrom = true;

        MemorySource outputMem = new MemorySource(w, outputSize);
        outputMem.canWriteTo = true;
        outputMem.canWriteToSpecificIndex = true;

        outputMem.sectors.get(inputSize).broken = true;

        return new Level("Avoid broken once", inputMem, outputMem, 3);
    }

    // TODO there is easy (one-if) pattern in broken sectors
    public static Level create3() {
        int w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        MemorySource inputMem = new MemorySource(w, inputSize);
        inputMem.canReadFrom = true;

        MemorySource outputMem = new MemorySource(w, outputSize);
        outputMem.canWriteTo = true;
        outputMem.canWriteToSpecificIndex = true;

        for (int i = inputSize; i < outputSize; i += inputSize) {
            outputMem.sectors.get(i).broken = true;
        }

        return new Level("Avoid broken ones", inputMem, outputMem, 3);
    }

    // there is pattern in broken sectors
    public static Level create4() {
        int w = 12, h = 15, outputSize = w * h, inputSize = w * 4;
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

        return new Level("The Pattern", inputMem, outputMem, 3);
    }
}
