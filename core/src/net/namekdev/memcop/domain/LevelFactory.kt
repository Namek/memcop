package net.namekdev.memcop.domain

import com.github.czyzby.kiwi.util.gdx.collection.GdxArrays

object LevelFactory {

    fun create(index: Int): Level {
        val l = levelDefinitions.get(index).invoke()
        l.reset()
        return l
    }


    val levelDefinitions: kotlin.Array<() -> Level> = arrayOf(
        // there are no broken sectors, copy the memory once
        {
            val w = 12
            val h = 15
            val outputSize = w * h
            val inputSize = w * 4

            val inputMem = MemorySource(w, inputSize)
            inputMem.canReadFrom = true

            val outputMem = MemorySource(w, outputSize)
            outputMem.canWriteTo = true
            outputMem.canWriteToSpecificIndex = true

            Level("Basics", inputMem, outputMem, 1)
        },

        // there are no broken sectors, copy same memory 3 times
        {
            val w = 12
            val h = 15
            val outputSize = w * h
            val inputSize = w * 4

            val inputMem = MemorySource(w, inputSize)
            inputMem.canReadFrom = true

            val outputMem = MemorySource(w, outputSize)
            outputMem.canWriteTo = true
            outputMem.canWriteToSpecificIndex = true

            Level("3 x Basics", inputMem, outputMem, 3)
        },

        // there is exactly one broken sector
        {
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

            Level("Avoid broken once", inputMem, outputMem, 3)
        },

        // TODO there is easy (one-if) pattern in broken sectors
        {
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

            Level("Avoid broken ones", inputMem, outputMem, 3)
        },

        // there is pattern in broken sectors
        {
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

            Level("The Pattern", inputMem, outputMem, 3)
        }
    )
}
