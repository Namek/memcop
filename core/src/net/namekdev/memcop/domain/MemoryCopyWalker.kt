package net.namekdev.memcop.domain

class MemoryCopyWalker(
    private val memSource: MemorySource,
    private val indexStart: Int,
    private val length: Int,
    private val times: Int
) : Iterator<Sector?> {
    private val totalToCopy = length * times

    private var index: Int = 0
    private var seqCopied: Int = 0
    private var totalCopied: Int = 0
    private var sector: Sector? = null
    var progress: Float = 0.toFloat()
        private set

    private var nextExplored = false
    private var hasNext = false


    init {
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