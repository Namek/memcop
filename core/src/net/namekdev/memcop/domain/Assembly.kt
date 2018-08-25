package net.namekdev.memcop.domain


import java.util.*
import java.util.regex.Pattern

import net.namekdev.memcop.domain.Assembly.OpArgType.*

object Assembly {

    val registers = arrayOf(
        reg("$0", true, false),
        reg("\$a", true, true),
        reg("\$b", true, true),
        reg("\$c", true, true),
        reg("\$d", true, true)
    )
    val registerByName = TreeMap<String, Register>()

    val Value = OpArg(arrayOf(Register, Const))
    val ALabel = OpArg(arrayOf(Label, Const))
    val OutRegister = OpArg(arrayOf(Register), true)
    val ARegister = OpArg(arrayOf(Register), false)

    val opdefs = arrayOf(
        op("mov", Value, OutRegister).describe("copy value: a -> b"),
        op("add", Value, Value, OutRegister).describe("a + b -> c"),
        op("sub", Value, Value, OutRegister).describe("a - b -> c"),
        op("mul", Value, Value, OutRegister).describe("a * b -> c"),
        op("div", Value, Value, OutRegister).describe("a / b -> c"),
        op("and", Value, Value, OutRegister).describe("a & b -> c"),
        op("orl", Value, Value, OutRegister).describe("a || b -> c"),
        op("xor", Value, Value, OutRegister).describe("a ^ b -> c"),
        op("inc", OutRegister).describe("increment value in register"),
        op("dec", OutRegister).describe("decrement value in register"),
        op("push", Value).describe("push value on stack"),
        op("pop", OutRegister).describe("remove top value from the stack and put it into specified register"),
        op("top", OutRegister).describe("get top stack value stack without removing it"),
        op("cmp", Value, Value).describe("compare two values and put 0, 1 or -1 into internal register, useful only for jumps"),
        op("ldi", OutRegister).describe("load one source number to specified register"),
        op("sti", ARegister, Value).describe("store number from given register to given index inside destination memory"),
        op("transfer", Value).describe("load byte and put it to specified index inside destination memory"),
        op("jump", ALabel).describe("unconditional jump"),
        op("je", ALabel).describe("jump if equal to"),
        op("jne", ALabel).describe("jump if not equal to"),
        op("jg", ALabel).describe("jump if right value is greater than left"),
        op("jl", ALabel).describe("jump if right value is lesser than than left"),
        op("jge", ALabel).describe("jump if right value is greater or equal to left"),
        op("jle", ALabel).describe("jump if right value is lesser or equal to left"))
    var opdefByName = TreeMap<String, OpDefinition>()

    private var _regsCreated = 0
    private var _opdefsCreated = 0


    private val labelPointRegex = Pattern.compile("([_a-zA-Z]+[_a-zA-Z0-9]+):")
    private val labelNameRegex = Pattern.compile("([_a-zA-Z]+[_a-zA-Z0-9]+)")
    private val constRegex = Pattern.compile("-?\\d+")
    private val registerRegex = Pattern.compile("\\$[a-z][a-z0-9]?")

    init {
        for (reg in registers)
            registerByName[reg.name] = reg

        for (oc in opdefs)
            opdefByName[oc.instructionName] = oc
    }

    class Register(val id: Int, public val name: String, var readable: Boolean, var writable: Boolean)

    class OpDefinition(val index: Int, val instructionName: String, val args: Array<OpArg>) {
        lateinit var description: String

        fun describe(description: String): OpDefinition {
            this.description = description
            return this
        }
    }

    class OpArg(val allowedTypes: Array<OpArgType>, val isReturnType: Boolean = false)

    enum class OpArgType {
        Register,
        Const,
        Label
    }


    class Instruction(val args: Array<InstrArg>, val opdef: OpDefinition, val lineText: String, val lineNumber: Int) {
        override fun toString(): String {
            val sb = StringBuilder(opdef.instructionName)

            for (arg in args) {
                sb.append(" ")
                sb.append(arg.token)
            }

            return sb.toString()
        }
    }

    class InstrArg(var argType: OpArgType, val token: String) {
        /**
         * depending on argType it is: constant value, register id or relative offset for label
         */
        var constVal = -1
    }

    class Label(val name: String, val line: Int) {
        var instructionIndex = -1
    }

    private fun reg(name: String, readable: Boolean, writable: Boolean): Register {
        return Register(_regsCreated++, name, readable, writable)
    }

    private fun op(str: String, vararg args: OpArg): OpDefinition {
        return OpDefinition(_opdefsCreated++, str, arrayOf(*args))
    }

    @Throws(Assembly.AssemblyCompilationError::class)
    fun compile(code: String, level: Level): List<Instruction> {
        val labels = TreeMap<String, Label>()

        // 0. TODO scan for comments, remove them

        // 1. scan for labels
        var t = Scanner(code)
        var line = 0
        while (t.hasNextLine()) {
            val token = t.nextLine().trim { it <= ' ' }
            line += 1

            val match = labelPointRegex.matcher(token)
            if (!match.matches())
                continue

            val labelName = match.group(1)
            val label = Label(labelName, line)

            if (labels.containsKey(labelName)) {
                val existingLabel = labels[labelName]
                throw AssemblyCompilationError("Label `" + labelName + "` is already defined in line " + existingLabel?.line, line)
            }

            labels[labelName] = label
        }

        // 2. compile assembly
        val instructions = ArrayList<Instruction>(line)
        line = 0
        t = Scanner(code)
        while (t.hasNextLine()) {
            val lineStr = t.nextLine().trim { it <= ' ' }
            line += 1

            // omit empty line
            if (lineStr.isEmpty())
                continue

            // update label's instruction index, then continue
            val labelMatch = labelPointRegex.matcher(lineStr)
            if (labelMatch.matches()) {
                val labelName = labelMatch.group(1)
                val label = labels[labelName]
                    ?: throw AssemblyCompilationError("Label `$labelName` was never defined", line)

                label.instructionIndex = instructions.size

                continue
            }

            // it's supposed to be a instruction line, analyze it
            val instrTokenizer = StringTokenizer(lineStr, " \t\n\r\u000C")
            val token = instrTokenizer.nextToken()
            val opdef = opdefByName[token] ?: throw AssemblyCompilationError("Unknown instruction `$token`", line)

            val args: Array<InstrArg> = opdef.args.mapIndexed { argIndex, opArg ->
                val token = instrTokenizer.nextToken()

                val instrArgType = (if (registerRegex.matcher(token).matches())
                    Register
                else if (constRegex.matcher(token).matches())
                    Const
                else if (labelNameRegex.matcher(token).matches()) Label else null)
                    ?: throw AssemblyCompilationError("unrecognized token `$token`", line)

                if (Arrays.binarySearch(opArg.allowedTypes, instrArgType) >= 0) {
                    val instrArg = InstrArg(instrArgType, token)

                    if (instrArgType == Const) {
                        instrArg.constVal = Integer.parseInt(token)
                    }
                    else if (instrArgType == Label) {
                        val label = labels[token]
                            ?: throw AssemblyCompilationError("Label `$token` is not defined", line)

                        // relative offset for label is calculated in the 3rd step
                    }
                    else if (instrArgType == Register) {
                        val reg = registerByName[token]
                            ?: throw AssemblyCompilationError("Register `$token` does not exist", line)

                        // TODO instruction: check whether it is readable or writable

                        instrArg.constVal = reg.id
                    }

                    instrArg
                }
                else {
                    val nth = if (argIndex == 0) "1st" else if (argIndex == 1) "2nd" else if (argIndex == 2) "3rd" else (argIndex + 1).toString() + "st"
                    val msg = "instruction " + opdef.instructionName + ": argument of type " + instrArgType + " is not allowed for " + nth + " argument"
                    throw AssemblyCompilationError(msg, line)
                }
            }.toTypedArray()


            val instr = Instruction(args, opdef, lineStr, line)

            instructions.add(instr)
        }

        // 3. replace labels with constants: calculate relative offsets
        var instrIndex = 0
        for (instr in instructions) {
            for (arg in instr.args) {
                if (arg.argType == Label) {
                    val label = labels[arg.token]!!
                    val relativeOffset = label.instructionIndex - instrIndex
                    arg.constVal = relativeOffset
                    arg.argType = Const
                }
            }

            instrIndex += 1
        }

        return instructions
    }


    class AssemblyCompilationError(message: String, line: Int) : Exception("Error in line  $line : $message")
}