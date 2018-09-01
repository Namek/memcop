package net.namekdev.memcop.domain

class Level(
    var name: String,
    var inputMem: MemorySource,
    var outputMem: MemorySource,
    indexStartInput: Int,
    indexStartOutput: Int,
    var copyLength: Int,
    copyTimes: Int,
    val inputMemTitle: String = "from Cartridge #529",
    val outputMemTitle: String = "to disk"
) {
    var copyTimes = 1
    var indexStartInput = 0
    var indexStartOutput = 0
    var validator: CompletionValidator

    val goalDescription: String
        get() {
            val sb = StringBuilder("Copy $copyLength source numbers ")
            if (copyTimes > 1) {
                sb.append(copyTimes)
                sb.append(" times")
            }
            sb.append('.')

            return sb.toString()
        }

    init {
        this.copyTimes = copyTimes
        this.indexStartInput = indexStartInput
        this.indexStartOutput = indexStartOutput
        this.validator = SequentialCopyValidator(indexStartInput, indexStartOutput, copyLength, copyTimes)
    }

    constructor(name: String, inputMem: MemorySource, outputMem: MemorySource, copyTimes: Int)
        : this(name, inputMem, outputMem, 0, 0, inputMem.validSectorsCount, copyTimes) {}

    fun reset() {
        inputMem.reset()
        outputMem.reset()

        val inputWalker = CopyWalker(inputMem, indexStartInput, copyLength, 1)
        val outputWalker = CopyWalker(outputMem, indexStartOutput, copyLength, copyTimes)

        while (outputWalker.hasNext()) {
            if (!inputWalker.hasNext())
                inputWalker.reset()

            val inputSector = inputWalker.next()
            val outputSector = outputWalker.next()

            val p = inputWalker.progress
            inputSector!!.markForGradient(p)
            outputSector!!.markForGradient(p)
        }
    }


    interface CompletionValidator {
        fun validateLevelCompletion(level: Level): Boolean
    }

    class SequentialCopyValidator(private val iInputStart: Int, private val iOutputStart: Int, private val length: Int, private val times: Int) : CompletionValidator {
        private val totalToCopy: Int

        init {
            this.totalToCopy = length * times
        }

        override fun validateLevelCompletion(level: Level): Boolean {
            val inputWalker = CopyWalker(level.inputMem, iInputStart, length, 1)
            val outputWalker = CopyWalker(level.outputMem, iOutputStart, length, times)

            val seqCopied = 0
            var totalCopied = 0

            do {
                val hasInput = inputWalker.hasNext()
                val hasOutput = outputWalker.hasNext()

                if (!hasInput || !hasOutput)
                    break

                val src = inputWalker.next()
                val dst = outputWalker.next()

                if (src!!.value() == dst!!.value()) {
                    ++totalCopied
                }
                else {
                    return false
                }

            } while (true)

            return totalCopied >= totalToCopy
        }
    }


    class CopyWalker(
        private val memSource: MemorySource,
        private val indexStart: Int,
        private val length: Int,
        private val times: Int
    ) : Iterator<Sector?> {
        private val totalToCopy: Int

        private var index: Int = 0
        private var seqCopied: Int = 0
        private var totalCopied: Int = 0
        private var sector: Sector? = null
        var progress: Float = 0.toFloat()
            private set

        private var nextExplored = false
        private var hasNext = false


        init {
            this.totalToCopy = length * times

            reset()
        }

        override fun hasNext(): Boolean {
            tryMoveForward()

            return hasNext
        }

        override fun next(): Sector? {
            tryMoveForward()
            nextExplored = false
            return sector
        }

        fun reset() {
            index = indexStart
            seqCopied = 0
            totalCopied = 0
            progress = 0f
            nextExplored = false
            hasNext = false
        }

        private fun tryMoveForward() {
            if (nextExplored)
                return

            nextExplored = true

            if (totalCopied >= totalToCopy) {
                hasNext = false
                return
            }

            if (index == length) {
                seqCopied = 0
            }

            sector = memSource.sectors.get(index++)

            while (sector!!.broken) {
                sector = memSource.sectors.get(index++)
            }
            ++seqCopied
            ++totalCopied

            progress = seqCopied / length.toFloat()

            hasNext = true
        }
    }
}
