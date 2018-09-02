package net.namekdev.memcop.domain

import net.namekdev.memcop.domain.pojo.SectorInfo
import java.util.Random

/**
 * in-game memory sector
 */
class Sector(val originallyBroken: Boolean) {

    /**
     * Have algorithm tried to write to it?
     */
    var written = false

    /**
     * Is this sector a bad sector?
     */
    var broken = false

    private var value: Int = 0
    var levelInputGradient = 0f
    var markedForGradient: Boolean = false

    val isWritable: Boolean
        get() = !broken

    init {
        reset()
    }

    fun reset() {
        written = false
        broken = originallyBroken
        value = if (broken) random.nextInt() else 0
        levelInputGradient = 0f
    }

    fun write(value: Int) {
        // let's ditch the value for now
        written = true
        this.value = value
    }

    fun value(): Int {
        return value
    }

    fun markForGradient(gradient: Float) {
        this.levelInputGradient = gradient
        this.markedForGradient = true
    }

    override fun toString(): String {
        if (written)
            return "_"

        return if (broken) "x" else " "
    }

    companion object {
        val random = Random()

        fun fromPojo(info: SectorInfo): Sector {
            return Sector(info.isBroken)
        }
    }
}
