package net.namekdev.memcop.domain

import net.namekdev.memcop.domain.pojo.CopyValidatorInfo


abstract class LevelCompletionValidator(var info: CopyValidatorInfo) {
    abstract fun validateLevelCompletion(level: Level): Boolean


    companion object {
        fun fromPojo(info: CopyValidatorInfo): LevelCompletionValidator {
            return SequentialCopyValidator(info)
        }
    }
}

class SequentialCopyValidator(info: CopyValidatorInfo) : LevelCompletionValidator(info) {
    private val totalToCopy = info.copyLength * info.copyTimes

    override fun validateLevelCompletion(level: Level): Boolean {
        val inputMem = level.memories[info.inputMemIndex]
        val outputMem = level.memories[info.outputMemIndex]

        val inputWalker = MemoryCopyWalker(inputMem, info.indexStartInput, info.copyLength, 1)
        val outputWalker = MemoryCopyWalker(outputMem, info.indexStartOutput, info.copyLength, info.copyTimes)

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