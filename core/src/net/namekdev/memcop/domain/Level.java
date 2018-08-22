package net.namekdev.memcop.domain;

import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays;

@SuppressWarnings("ALL")
public class Level {
    public static final char EMPTY_SECTOR = ' ';
    public static final char BAD_SECTOR = '✕';

    public interface CompletionValidator {
        boolean validateLevelCompletion(Level level);
    }

    public static class SequentialCopyValidator implements CompletionValidator {
        private final int iInputStart;
        private final int iOutputStart;
        private final int length;
        private final int times;

        public SequentialCopyValidator(int iInputStart, int iOutputStart, int length, int times) {
            this.iInputStart = iInputStart;
            this.iOutputStart = iOutputStart;
            this.length = length;
            this.times = times;
        }

        @Override
        public boolean validateLevelCompletion(Level level) {
            final int totalToCopy = length * times;

            int spaceLeft = 0;
            int iSrc = 0, iDst = 0, seqCopied = 0, totalCopied = 0;
            for (; totalCopied < totalToCopy;) {
                Sector src = level.inputMem.sectors.get(iSrc);
                Sector dst = level.outputMem.sectors.get(iDst);

                while (!src.broken) {
                    src = level.inputMem.sectors.get(++iSrc);
                }

                while (!dst.broken) {
                    dst = level.inputMem.sectors.get(++iDst);
                }

                if (src.value() == dst.value()) {
                    ++seqCopied;
                    ++totalCopied;
                }
                else {
                    return false;
                }

                if (seqCopied == length) {
                    iSrc = 0;
                }
            }

            return totalCopied >= totalToCopy;
        }
    }

    public MemorySource inputMem;
    public MemorySource outputMem;
    public CompletionValidator validator;

    public Level(MemorySource inputMem, MemorySource outputMem, CompletionValidator validator) {
        this.inputMem = inputMem;
        this.outputMem = outputMem;
        this.validator = validator;
    }

    public Level(MemorySource inputMem, MemorySource outputMem, int copyTimes) {
        this(inputMem, outputMem, new SequentialCopyValidator(0, 0, inputMem.validSectorsCount, copyTimes));
    }

    public void reset() {
        inputMem.reset();
        outputMem.reset();
    }

    public static Level create(int index) {
        switch (index) {
            case 0: return create0();
            case 1: return create1();
            case 2: return create2();
            case 3: return create3();
            case 4: return create4();
            default: throw new Error("unknown level number: " + index);
        }
    }

    // there are no broken sectors, copy the memory once
    public static Level create0() {
        int w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        MemorySource inputMem = new MemorySource(w, inputSize);
        inputMem.canReadFrom = true;

        MemorySource outputMem = new MemorySource(w, outputSize);
        outputMem.canWriteTo = true;
        outputMem.canWriteToSpecificIndex = true;

        return new Level(inputMem, outputMem, 1);
    }


    // there are no broken sectors, copy same memory 3 times
    public static Level create1() {
        int w = 12, h = 15, outputSize = w * h, inputSize = w * 4;

        MemorySource inputMem = new MemorySource(w, inputSize);
        inputMem.canReadFrom = true;

        MemorySource outputMem = new MemorySource(w, outputSize);
        outputMem.canWriteTo = true;
        outputMem.canWriteToSpecificIndex = true;

        return new Level(inputMem, outputMem, 3);
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

        return new Level(inputMem, outputMem, 3);
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

        return new Level(inputMem, outputMem, 3);
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

        return new Level(inputMem, outputMem, 3);
    }

}
