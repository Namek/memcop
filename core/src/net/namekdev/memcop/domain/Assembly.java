package net.namekdev.memcop.domain;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.namekdev.memcop.domain.Assembly.OpArgType.*;

public abstract class Assembly {

    public static final Register[] registers = new Register[] {
        reg("$0", true, false),
        reg("$a", true, true),
        reg("$b", true, true),
        reg("$c", true, true),
        reg("$d", true, true)
    };
    public static TreeMap<String, Register> registerByName = new TreeMap<String, Register>();

    public static final OpArg $Value = new OpArg(new OpArgType[] {Register, Const});
    public static final OpArg $Label = new OpArg(new OpArgType[] {Label, Const});
    public static final OpArg $OutRegister = new OpArg(new OpArgType[] {Register}, true);
    public static final OpArg $Register = new OpArg(new OpArgType[] {Register}, false);

    public static final OpDefinition[] opdefs = new OpDefinition[] {
        op("mov", $Value, $OutRegister).describe("copy value: a -> b"),
        op("add", $Value, $Value, $OutRegister).describe("a + b -> c"),
        op("sub", $Value, $Value, $OutRegister).describe("a - b -> c"),
        op("mul", $Value, $Value, $OutRegister).describe("a * b -> c"),
        op("div", $Value, $Value, $OutRegister).describe("a / b -> c"),
        op("and", $Value, $Value, $OutRegister).describe("a & b -> c"),
        op("orl", $Value, $Value, $OutRegister).describe("a || b -> c"),
        op("xor", $Value, $Value, $OutRegister).describe("a ^ b -> c"),
        op("push", $Value).describe("push value on stack"),
        op("pop", $OutRegister).describe("remove top value from the stack and put it into specified register"),
        op("top", $OutRegister).describe("get top stack value stack without removing it"),
        op("cmp", $Value, $Value).describe("compare two values and put 0, 1 or -1 into internal register, useful only for jumps"),
        op("ldi", $OutRegister).describe("load one source number to specified register"),
        op("sti", $Register, $Value).describe("store number from given register to given index inside destination memory"),
        op("transfer", $Value).describe("load byte and put it to specified index inside destination memory"),
        op("jump", $Label).describe("unconditional jump"),
        op("je", $Label).describe("jump if equal to"),
        op("jne", $Label).describe("jump if not equal to"),
        op("jg", $Label).describe("jump if right value is greater than left"),
        op("jl", $Label).describe("jump if right value is lesser than than left"),
        op("jge", $Label).describe("jump if right value is greater or equal to left"),
        op("jle", $Label).describe("jump if right value is lesser or equal to left"),
    };
    public static TreeMap<String, OpDefinition> opdefByName = new TreeMap<String, OpDefinition>();

    static {
        for (Register reg : registers)
            registerByName.put(reg.name, reg);

        for (OpDefinition oc : opdefs)
            opdefByName.put(oc.instructionName, oc);
    }

    public static class Register {
        public final int id;
        public final String name;
        public boolean readable;
        public boolean writable;

        public Register(int id, String name, boolean readable, boolean writable) {
            this.id = id;
            this.name = name;
            this.readable = readable;
            this.writable = writable;
        }
    }

    public static class OpDefinition {
        public final int id;
        public final String instructionName;
        public final OpArg[] args;
        public String description;

        public OpDefinition(int id, String name, OpArg[] args) {
            this.id = id;
            this.instructionName = name;
            this.args = args;
        }

        public OpDefinition describe(String description)
        {
            this.description = description;
            return this;
        }
    }

    public static class OpArg {
        public final OpArgType[] allowedTypes;
        public final boolean isReturnType;

        public OpArg(OpArgType[] opArgTypes) {
            this.allowedTypes = opArgTypes;
            this.isReturnType = false;
        }

        public OpArg(OpArgType[] opArgTypes, boolean isOut) {
            this.allowedTypes = opArgTypes;
            this.isReturnType = isOut;
        }
    }

    public enum OpArgType {
        Register,
        Const,
        Label;
    }


    public static class Instruction {
        public OpDefinition opdef;
        public InstrArg[] args;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(opdef.instructionName);

            for (InstrArg arg : args) {
                sb.append(" ");
                sb.append(arg.token);
            }

            return sb.toString();
        }
    }

    public static class InstrArg {
        public OpArgType argType;
        public String token;

        /**
         * depending on argType it is: constant value, register id or relative offset for label
         */
        public int constVal = -1;
    }

    public static class Label {
        public String name;
        public int line = -1;
        public int instructionIndex = -1;
    }

    private static int _regsCreated = 0;
    private static Register reg(String name, boolean readable, boolean writable) {
        return new Register(_regsCreated++, name, readable, writable);
    }

    private static int _opdefsCreated = 0;
    private static OpDefinition op(String str, OpArg... args) {
        return new OpDefinition(_opdefsCreated++, str, args);
    }


    private static Pattern labelPointRegex = Pattern.compile("([_a-zA-Z]+[_a-zA-Z0-9]+):");
    private static Pattern labelNameRegex = Pattern.compile("([_a-zA-Z]+[_a-zA-Z0-9]+)");
    private static Pattern constRegex = Pattern.compile("-?\\d+");
    private static Pattern registerRegex = Pattern.compile("\\$[a-z][a-z0-9]?");

    public static List<Instruction> compile(String code, Level level) throws AssemblyCompilationError {
        final TreeMap<String, Label> labels = new TreeMap<String, Label>();

        // 1. scan for labels
        StringTokenizer t = new StringTokenizer(code, "\r\n");
        int line = 0;
        while (t.hasMoreTokens()) {
            String token = t.nextToken().trim();
            line += 1;

            Matcher match = labelPointRegex.matcher(token);
            if (!match.matches())
                continue;

            String labelName = match.group(1);
            Label label = new Label();
            label.name = labelName;
            label.line = line;

            if (labels.containsKey(labelName)) {
                Label existingLabel = labels.get(labelName);
                throw new AssemblyCompilationError("Label `" + labelName + "` is already defined in line " + existingLabel.line, line);
            }

            labels.put(labelName, label);
        }

        // 2. compile assembly
        final List<Instruction> instructions = new ArrayList<Instruction>(line);
        line = 0;
        t = new StringTokenizer(code, "\r\n");
        while (t.hasMoreTokens())
        {
            String lineStr = t.nextToken().trim();
            line += 1;

            // omit empty line
            if (lineStr.length() == 0)
                continue;

            // update label's instruction index, then continue
            Matcher labelMatch = labelPointRegex.matcher(lineStr);
            if (labelMatch.matches()) {
                String labelName = labelMatch.group(1);
                Label label = labels.get(labelName);

                if (label == null)
                    throw new AssemblyCompilationError("Label `" + labelName + "` was never defined", line);

                label.instructionIndex = instructions.size();

                continue;
            }

            // it's supposed to be a instruction line, analyze it
            StringTokenizer instrTokenizer = new StringTokenizer(lineStr, " \t\n\r\f");
            String token = instrTokenizer.nextToken();

            OpDefinition opdef = opdefByName.get(token);

            if (opdef == null)
                throw new AssemblyCompilationError("Unknown instruction `" + token + "`", line);

            Instruction instr = new Instruction();
            instr.opdef = opdef;
            instr.args = new InstrArg[opdef.args.length];

            int argIndex = 0;
            for (OpArg opArg : opdef.args) {
                token = instrTokenizer.nextToken();

                OpArgType instrArgType = registerRegex.matcher(token).matches() ? Register :
                        constRegex.matcher(token).matches() ? Const :
                        labelNameRegex.matcher(token).matches() ? Label : null;

                if (instrArgType == null) {
                    throw new AssemblyCompilationError("unrecognized token `" + token + "`", line);
                }

                if (Arrays.binarySearch(opArg.allowedTypes, instrArgType) >= 0) {
                    InstrArg instrArg = new InstrArg();
                    instrArg.token = token;
                    instrArg.argType = instrArgType;

                    if (instrArgType == Const) {
                        instrArg.constVal = Integer.parseInt(token);
                    }
                    else if (instrArgType == Label) {
                        Label label = labels.get(token);

                        if (label == null)
                            throw new AssemblyCompilationError("Label `" + token + "` is not defined", line);

                        // relative offset for label is calculated in the 3rd step
                    }
                    else if (instrArgType == Register) {
                        Register reg = registerByName.get(token);

                        if (reg == null)
                            throw new AssemblyCompilationError("Register `" + token + "` does not exist", line);

                        // TODO instruction: check whether it is readable or writable

                        instrArg.constVal = reg.id;
                    }

                    instr.args[argIndex++] = instrArg;
                }
                else {
                    String nth = argIndex == 0 ? "1st" : argIndex == 1 ? "2nd" : argIndex == 2 ? "3rd" : (argIndex+1) + "st";
                    String msg = "instruction " + opdef.instructionName + ": argument of type " + instrArgType + " is not allowed for " + nth + " argument";
                    throw new AssemblyCompilationError(msg, line);
                }
            }

            instructions.add(instr);
        }

        // 3. replace labels with constants: calculate relative offsets
        int instrIndex = 0;
        for (Instruction instr : instructions) {
            for (InstrArg arg : instr.args) {
                if (arg.argType == Label) {
                    Label label = labels.get(arg.token);
                    int relativeOffset = label.instructionIndex - instrIndex;
                    arg.constVal = relativeOffset;
                    arg.argType = Const;
                }
            }

            instrIndex += 1;
        }

        return instructions;
    }


    public static class AssemblyCompilationError extends Exception {
        public AssemblyCompilationError(String message, int line) {
            super("Error in line  " + line + " : " + message);
        }
    }
}
