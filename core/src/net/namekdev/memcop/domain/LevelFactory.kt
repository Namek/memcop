package net.namekdev.memcop.domain

import com.badlogic.gdx.utils.Array
import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays

object LevelFactory {

    fun create(index: Int): Level {
        val l: Level
        when (index) {
            0 -> l = create0()
            1 -> l = create1()
            2 -> l = create2()
            3 -> l = create3()
            4 -> l = create4()
            else -> throw Error("unknown level number: $index")
        }
        l.reset()

        return l
    }


    // there are no broken sectors, copy the memory once
    fun create0(): Level {
        val w = 12
        val h = 15
        val outputSize = w * h
        val inputSize = w * 4

        val inputMem = MemorySource(w, inputSize)
        inputMem.canReadFrom = true

        val outputMem = MemorySource(w, outputSize)
        outputMem.canWriteTo = true
        outputMem.canWriteToSpecificIndex = true

        return Level("Basics", inputMem, outputMem, 1)
    }


    // there are no broken sectors, copy same memory 3 times
    fun create1(): Level {
        val w = 12
        val h = 15
        val outputSize = w * h
        val inputSize = w * 4

        val inputMem = MemorySource(w, inputSize)
        inputMem.canReadFrom = true

        val outputMem = MemorySource(w, outputSize)
        outputMem.canWriteTo = true
        outputMem.canWriteToSpecificIndex = true

        return Level("3 x Basics", inputMem, outputMem, 3)
    }

    // there is exactly one broken sector
    fun create2(): Level {
        val w = 12
        val h = 15
        val outputSize = w * h
        val inputSize = w * 4

        val inputMem = MemorySource(w, inputSize)
        inputMem.canReadFrom = true

        val outputMem = MemorySource(w, outputSize)
        outputMem.canWriteTo = true
        outputMem.canWriteToSpecificIndex = true

        outputMem.sectors.get(inputSize).broken = true

        return Level("Avoid broken once", inputMem, outputMem, 3)
    }

    // TODO there is easy (one-if) pattern in broken sectors
    fun create3(): Level {
        val w = 12
        val h = 15
        val outputSize = w * h
        val inputSize = w * 4

        val inputMem = MemorySource(w, inputSize)
        inputMem.canReadFrom = true

        val outputMem = MemorySource(w, outputSize)
        outputMem.canWriteTo = true
        outputMem.canWriteToSpecificIndex = true

        var i = inputSize
        while (i < outputSize) {
            outputMem.sectors.get(i).broken = true
            i += inputSize
        }

        return Level("Avoid broken ones", inputMem, outputMem, 3)
    }

    // there is pattern in broken sectors
    fun create4(): Level {
        val w = 12
        val h = 15
        val outputSize = w * h
        val inputSize = w * 4
        val sectors = GdxArrays.newArray<Sector>(outputSize)
        sectors.setSize(outputSize)
        for (i in 0 until outputSize)
            sectors.set(i, Sector())

        var y = 0
        var padLeft = false
        var brokenSectors = 0

        do {
            var startX = 1
            y += 1

            if (padLeft)
                startX += 1

            var i = y * w + startX
            if (i >= outputSize) break
            sectors.set(i, Sector.newBroken())
            brokenSectors += 1

            i += 3
            if (i >= outputSize) break
            sectors.set(i, Sector.newBroken())
            brokenSectors += 1

            i += 5
            if (i >= outputSize) break
            sectors.set(i, Sector.newBroken())
            brokenSectors += 1

            y += 1
            padLeft = !padLeft
        } while (y < h)

        val inputMem = MemorySource(w, inputSize)
        inputMem.canReadFrom = true

        val outputMem = MemorySource(w, sectors)
        outputMem.canWriteTo = true
        outputMem.canWriteToSpecificIndex = true

        return Level("The Pattern", inputMem, outputMem, 3)
    }
}
