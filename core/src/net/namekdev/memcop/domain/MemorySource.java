package net.namekdev.memcop.domain;

import com.badlogic.gdx.utils.Array;

public class MemorySource {
    public final int validSectorsCount;
    public final int sectorsPerRow;
    public final Array<Sector> sectors;

    public boolean canReadFrom;
    public boolean canWriteTo;
    public boolean canWriteToSpecificIndex;
    public boolean canReadFromSpecificIndex;

    private int lastPos = -1;

    public MemorySource(int sectorsPerRow, Array<Sector> sectors) {
        this.sectorsPerRow = sectorsPerRow;
        this.sectors = sectors;

        int i = 0;
        for (Sector sector : sectors) {
            if (sector.isWritable())
                i++;
        }
        this.validSectorsCount = i;
    }

    public MemorySource(int sectorsPerRow, int totalSize) {
        Array<Sector> sectors = new Array<Sector>(totalSize);
        for (int i = 0; i < totalSize; ++i)
            sectors.add(new Sector());

        this.sectorsPerRow = sectorsPerRow;
        this.sectors = sectors;
        this.validSectorsCount = totalSize;
    }

    public int readValue(int requestedIndex) {
        if (!canReadFrom)
            throw new Error("this memory can't be read from");

        int index = canReadFromSpecificIndex ? requestedIndex : ++lastPos;

        return (byte) sectors.get(index).value();
    }

    public boolean saveByte(int destinationIndex, int value) {
        if (!canWriteTo)
            throw new Error("can't write to this memory");

        int index = canWriteToSpecificIndex ? destinationIndex : ++lastPos;
        Sector sector = sectors.get(index);

        if (sector == null)
            return false;

        sector.write(value);
        return true;
    }

    public int getTotalHeight() {
        int t = sectors.size % sectorsPerRow == 0 ? 0 : 1;
        return sectors.size / sectorsPerRow + t;
    }


    public void reset() {
        lastPos = -1;
        for (Sector sector : sectors)
            sector.reset();
    }
}
