package net.namekdev.memcop.domain

class Level(
    val name: String,
    val goalDescription: String,
    val memories: List<MemorySource>,
    val validators: List<LevelCompletionValidator>,
    val shouldCopyCodeSolutionFromPreviousLevel: Boolean
) {
    fun reset() {
        for (validator in validators) {
            val info = validator.info
            val inputMem = memories[info.inputMemIndex]
            val outputMem = memories[info.outputMemIndex]

            inputMem.reset()
            outputMem.reset()

            val inputWalker = MemoryCopyWalker(inputMem, info.indexStartInput, info.copyLength, 1)
            val outputWalker = MemoryCopyWalker(outputMem, info.indexStartOutput, info.copyLength, info.copyTimes)

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
    }
}
