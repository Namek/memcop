package net.namekdev.memcop.domain

import net.namekdev.memcop.domain.pojo.MemorySourceInfo

class MemorySource(
    val title: String,
    val sectorsPerRow: Int,
    val sectors: Array<Sector>
) {
    val validSectorsCount: Int

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

    init {
        var i = 0
        for (sector in sectors) {
            if (sector.isWritable)
                i++
        }
        this.validSectorsCount = i
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

    companion object {
        fun fromPojo(info: MemorySourceInfo): MemorySource {
            val sectors: Array<Sector> = (
                if (info.sectors != null)
                    info.sectors.map { it -> Sector.fromPojo(it) }.toTypedArray()
                else {
                    if (info.brokenSectorIndices == null) {
                        Array(info.size) { Sector(false) }
                    }
                    else Array(info.size) { i ->
                        val isBroken = info.brokenSectorIndices.contains(i)
                        Sector(isBroken)
                    }
                }
            )

            val mem = MemorySource(info.title, info.sectorsPerRow, sectors)
            mem.canReadFrom = info.canReadFrom
            mem.canReadFromSpecificIndex = info.canReadFromSpecificIndex
            mem.canWriteTo = info.canWriteTo
            mem.canWriteToSpecificIndex = info.canWriteToSpecificIndex

            return mem
        }
    }
}
