package net.namekdev.memcop.domain

import com.badlogic.gdx.utils.Array

class MemorySource {
    val validSectorsCount: Int
    val sectorsPerRow: Int
    val sectors: Array<Sector>

    var canReadFrom: Boolean = false
    var canWriteTo: Boolean = false
    var canWriteToSpecificIndex: Boolean = false
    var canReadFromSpecificIndex: Boolean = false

    var lastPos = -1
        private set

    val totalHeight: Int
        get() {
            val t = if (sectors.size % sectorsPerRow == 0) 0 else 1
            return sectors.size / sectorsPerRow + t
        }

    constructor(sectorsPerRow: Int, sectors: Array<Sector>) {
        this.sectorsPerRow = sectorsPerRow
        this.sectors = sectors

        var i = 0
        for (sector in sectors) {
            if (sector.isWritable)
                i++
        }
        this.validSectorsCount = i
    }

    constructor(sectorsPerRow: Int, totalSize: Int) {
        val sectors = Array<Sector>(totalSize)
        for (i in 0 until totalSize)
            sectors.add(Sector())

        this.sectorsPerRow = sectorsPerRow
        this.sectors = sectors
        this.validSectorsCount = totalSize
    }

    fun readValue(requestedIndex: Int): Int {
        if (!canReadFrom)
            throw Error("this memory can't be read from")

        val index = if (canReadFromSpecificIndex) requestedIndex else ++lastPos
        lastPos = index

        return sectors.get(index).value().toByte().toInt()
    }

    fun saveByte(destinationIndex: Int, value: Int): Boolean {
        if (!canWriteTo)
            throw Error("can't write to this memory")

        val index = if (canWriteToSpecificIndex) destinationIndex else ++lastPos
        val sector = sectors.get(index) ?: return false

        lastPos = index
        sector.write(value)
        return true
    }


    fun reset() {
        lastPos = -1
        for (sector in sectors)
            sector.reset()
    }
}
