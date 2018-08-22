package net.namekdev.memcop.domain;

import com.badlogic.gdx.utils.Array;
import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays;

import java.util.Iterator;

@SuppressWarnings("ALL")
public class Level {
    public static final char EMPTY_SECTOR = ' ';
    public static final char BAD_SECTOR = '✕';

    public MemorySource inputMem;
    public MemorySource outputMem;
    public int copyTimes = 1;
    public int copyLength;
    public int indexStartInput = 0;
    public int indexStartOutput = 0;
    public CompletionValidator validator;

    public Level(MemorySource inputMem, MemorySource outputMem, int indexStartInput, int indexStartOutput, int copyLength, int copyTimes) {
        this.inputMem = inputMem;
        this.outputMem = outputMem;
        this.copyLength = copyLength;
        this.copyTimes = copyTimes;
        this.indexStartInput = indexStartInput;
        this.indexStartOutput = indexStartOutput;
        this.validator = new SequentialCopyValidator(indexStartInput, indexStartOutput, copyLength, copyTimes);
    }

    public Level(MemorySource inputMem, MemorySource outputMem, int copyTimes) {
        this(inputMem, outputMem, 0, 0, inputMem.validSectorsCount, copyTimes);
    }

    public void reset() {
        inputMem.reset();
        outputMem.reset();

        final CopyWalker inputWalker = new CopyWalker(inputMem, indexStartInput, copyLength, 1);
        final CopyWalker outputWalker = new CopyWalker(outputMem, indexStartOutput, copyLength, copyTimes);

        while (outputWalker.hasNext()) {
            if (!inputWalker.hasNext())
                inputWalker.reset();

            Sector inputSector = inputWalker.next();
            Sector outputSector = outputWalker.next();

            float p = inputWalker.getProgress();
            inputSector.markForGradient(p);
            outputSector.markForGradient(p);
        }
    }

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


    public interface CompletionValidator {
        boolean validateLevelCompletion(Level level);
    }

    public static class SequentialCopyValidator implements CompletionValidator {
        private final int iInputStart;
        private final int iOutputStart;
        private final int length;
        private final int times;
        private final int totalToCopy;

        public SequentialCopyValidator(int iInputStart, int iOutputStart, int length, int times) {
            this.iInputStart = iInputStart;
            this.iOutputStart = iOutputStart;
            this.length = length;
            this.times = times;
            this.totalToCopy = length * times;
        }

        @Override
        public boolean validateLevelCompletion(Level level) {
            final CopyWalker inputWalker = new CopyWalker(level.inputMem, iInputStart, length, 1);
            final CopyWalker outputWalker = new CopyWalker(level.outputMem, iOutputStart, length, times);

            int seqCopied = 0, totalCopied = 0;

            do {
                boolean hasInput = inputWalker.hasNext();
                boolean hasOutput = outputWalker.hasNext();

                if (!hasInput || !hasOutput)
                    break;

                Sector src = inputWalker.next();
                Sector dst = outputWalker.next();

                if (src.value() == dst.value()) {
                    ++totalCopied;
                }
                else {
                    return false;
                }

            } while (true);

            return totalCopied >= totalToCopy;
        }
    }


    public static class CopyWalker implements Iterator<Sector> {
        private final MemorySource memSource;
        private final int indexStart;
        private final int length;
        private final int times;
        private final int totalToCopy;

        private int index, seqCopied, totalCopied;
        private Sector sector = null;
        private float progress;

        private boolean nextExplored = false, hasNext = false;


        public CopyWalker(MemorySource memSource, int indexStart, int length, int times) {
            this.memSource = memSource;
            this.indexStart = indexStart;
            this.length = length;
            this.times = times;
            this.totalToCopy = length * times;

            reset();
        }

        @Override
        public boolean hasNext() {
            tryMoveForward();

            return hasNext;
        }

        @Override
        public Sector next() {
            tryMoveForward();
            nextExplored = false;
            return sector;
        }

        public float getProgress() {
            return progress;
        }

        public void reset() {
            index = indexStart;
            seqCopied = 0;
            totalCopied = 0;
            progress = 0;
            nextExplored = false;
            hasNext = false;
        }

        private void tryMoveForward() {
            if (nextExplored)
                return;

            nextExplored = true;

            if (totalCopied >= totalToCopy) {
                hasNext = false;
                return;
            }

            if (index == length) {
                seqCopied = 0;
            }

            sector = memSource.sectors.get(index++);

            while (sector.broken) {
                sector = memSource.sectors.get(index++);
            }
            ++seqCopied;
            ++totalCopied;

            progress = seqCopied / (float)length;

            hasNext = true;
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
