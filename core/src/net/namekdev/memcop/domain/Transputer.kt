package net.namekdev.memcop.domain

import java.util.Stack
import java.util.TreeMap
import net.namekdev.memcop.domain.Assembly.Instruction
import net.namekdev.memcop.domain.Assembly.InstrArg
import net.namekdev.memcop.domain.Assembly.OpArgType

/**
 * @return new cursor or -1 if instruction don't want to jump
 */
typealias InstrExecutor = (Instruction) -> Int

class Transputer {

    var sourceMemory: MemorySource? = null
    var destMemory: MemorySource? = null

    /**
     * Defines a position before given instruction is executed.
     */
    var nextInstrIndex = -1
    var instructions: List<Instruction>? = null
    var lastInstruction: Instruction? = null
    var stack = Stack<Int>()
    var stackMaxSize = 4
    val registerStates: Array<RegisterState>
    var lastComparison = 0



    private val instructionExecutors: Array<InstrExecutor>
    private val instructionExecutorByName = object : TreeMap<String, InstrExecutor>() {
        init {
            put("mov") { instr ->
                val src = getValue(instr.args[0])
                getReg(instr.args[1]).value = src

                -1
            }
            put("add") { instr ->
                val value1 = getValue(instr.args[0])
                val value2 = getValue(instr.args[1])
                getReg(instr.args[2]).value = value1 + value2

                -1
            }
            put("sub") { instr ->
                val value1 = getValue(instr.args[0])
                val value2 = getValue(instr.args[1])
                getReg(instr.args[2]).value = value1 - value2

                -1
            }
            put("mul") { instr ->
                val value1 = getValue(instr.args[0])
                val value2 = getValue(instr.args[1])
                getReg(instr.args[2]).value = value1 * value2

                -1
            }
            put("div") { instr ->
                val value1 = getValue(instr.args[0])
                val value2 = getValue(instr.args[1])
                getReg(instr.args[2]).value = value1 / value2

                -1
            }
            put("and") { instr ->
                val value1 = getValue(instr.args[0])
                val value2 = getValue(instr.args[1])
                getReg(instr.args[2]).value = value1 and value2

                -1
            }
            put("orl") { instr ->
                val value1 = getValue(instr.args[0])
                val value2 = getValue(instr.args[1])
                getReg(instr.args[2]).value = value1 or value2

                -1
            }
            put("xor") { instr ->
                val value1 = getValue(instr.args[0])
                val value2 = getValue(instr.args[1])
                getReg(instr.args[2]).value = value1 xor value2

                -1
            }
            put("inc") { instr ->
                getReg(instr.args[0]).value += 1
                -1
            }
            put("dec") { instr ->
                getReg(instr.args[0]).value -= 1
                -1
            }
            put("push") { instr ->
                val value = getValue(instr.args[0])
                stack.push(value)

                if (stack.size >= stackMaxSize)
                    stack.setSize(stackMaxSize)

                -1
            }
            put("pop") { instr ->
                val reg = getReg(instr.args[0])

                var value = 0
                if (!stack.isEmpty())
                    value = stack.pop()

                reg.value = value

                -1
            }
            put("top") { instr ->
                val reg = getReg(instr.args[0])

                var value = 0
                if (!stack.isEmpty())
                    value = stack.peek()

                reg.value = value

                -1
            }
            put("cmp") { instr ->
                val value1 = getValue(instr.args[0])
                val value2 = getValue(instr.args[1])

                val cmp = value2 - value1
                lastComparison = if (cmp > 0) 1 else if (cmp < 0) -1 else 0

                -1
            }
            put("ldi") { instr ->
                val reg = getReg(instr.args[0])
                reg.value = loadSourceByte()

                -1
            }
            put("sti") { instr ->
                val srcByte = getValue(instr.args[0])
                val destIndex = getValue(instr.args[1])
                storeDestinationByte(destIndex, srcByte)

                 -1
            }
            put("transfer") { instr ->
                val destIndex = getValue(instr.args[0])
                val srcByte = loadSourceByte()
                storeDestinationByte(destIndex, srcByte)

                 -1
            }
            put("jump") { instr ->
                val relativeOffset = getValue(instr.args[0])
                 nextInstrIndex + relativeOffset
            }
            put("je") { instr ->
                val relativeOffset = getValue(instr.args[0])
                 if (lastComparison == EQUAL) nextInstrIndex + relativeOffset else -1
            }
            put("jne") { instr ->
                val relativeOffset = getValue(instr.args[0])
                if (lastComparison != EQUAL) nextInstrIndex + relativeOffset else -1
            }
            put("jg") { instr ->
                val relativeOffset = getValue(instr.args[0])
                if (lastComparison == GREATER_THAN) nextInstrIndex + relativeOffset else -1
            }
            put("jl") { instr ->
                val relativeOffset = getValue(instr.args[0])
                if (lastComparison == LESSER_THAN) nextInstrIndex + relativeOffset else -1
            }
            put("jge") { instr ->
                val relativeOffset = getValue(instr.args[0])
                if (lastComparison != LESSER_THAN) nextInstrIndex + relativeOffset else -1
            }
            put("jle") { instr ->
                val relativeOffset = getValue(instr.args[0])
                if (lastComparison != GREATER_THAN) nextInstrIndex + relativeOffset else -1
            }
        }
    }

    val isDebuggingActive: Boolean
        get() = nextInstrIndex >= 0

    val nextInstruction: Instruction?
        get() =
            if (nextInstrIndex < 0 || nextInstrIndex >= instructions!!.size) null
            else instructions!![nextInstrIndex]


    inner class RegisterState(val info: Assembly.Register) {
        var value: Int = 0

        override fun toString(): String {
            return info.name + " = " + value
        }
    }


    init {
        // initialize registers
        registerStates = Assembly.registers
            .map { RegisterState(it) }
            .toTypedArray()

        // initialize instructions
        instructionExecutors = {
            val arr = ArrayList<InstrExecutor>()
            arr.ensureCapacity(Assembly.opdefs.size)

            for (i in 0 until Assembly.opdefs.size) {
                val opdef = Assembly.opdefs[i]
                val executor = instructionExecutorByName[opdef.instructionName]

                if (executor == null)
                    throw Error("instruction `" + opdef.instructionName + "` has not been implemented")

                assert(opdef.index == i)
                arr.add(executor)
            }
            arr.toTypedArray()
        }()
    }

    fun startDebugging() {
        reset()
        nextInstrIndex = 0
    }

    /**
     * Execute a single instruction.
     */
    fun step(): Boolean {
        if (nextInstrIndex < 0)
            return false

        if (nextInstrIndex >= instructions!!.size) {
            nextInstrIndex = instructions!!.size + 1
            return false
        }

        val instr = instructions!![nextInstrIndex]
        val instrExec = instructionExecutors[instr.opdef!!.index]
        val newInstrCursor = instrExec(instr)

        if (newInstrCursor >= 0)
            nextInstrIndex = newInstrCursor
        else
            nextInstrIndex += 1

        lastInstruction = instr
        return true
    }

    fun reset() {
        nextInstrIndex = -1
        for (reg in registerStates) {
            reg.value = 0
        }
        lastComparison = 0
        lastInstruction = null
        stack.clear()
    }

    private fun getValue(instrArg: InstrArg): Int {
        val value: Int

        if (instrArg.argType === OpArgType.Register) {
            val regId = instrArg.constVal
            val reg = registerStates[regId]
            value = reg.value
        }
        else {
            value = instrArg.constVal
        }

        return value
    }

    private fun getReg(instrArg: InstrArg): RegisterState {
        if (instrArg.argType !== OpArgType.Register)
            throw Error("expected register address")

        return registerStates[instrArg.constVal]
    }

    private fun storeDestinationByte(destIndex: Int, srcByte: Int) {
        destMemory!!.saveByte(destIndex, srcByte.toByte().toInt())
    }

    private fun loadSourceByte(): Int {
        return sourceMemory!!.readValue(-1)
    }

    companion object {
        val EQUAL = 0
        val LESSER_THAN = -1
        val GREATER_THAN = 1
    }
}
