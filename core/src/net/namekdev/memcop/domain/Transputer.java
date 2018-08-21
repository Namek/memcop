package net.namekdev.memcop.domain;

import java.util.List;
import java.util.Stack;
import java.util.TreeMap;
import net.namekdev.memcop.domain.Assembly.Instruction;
import net.namekdev.memcop.domain.Assembly.InstrArg;
import net.namekdev.memcop.domain.Assembly.OpArgType;
import net.namekdev.memcop.domain.Assembly.OpDefinition;

public class Transputer {
    public static final int EQUAL = 0;
    public static final int LESSER_THAN = -1;
    public static final int GREATER_THAN = 1;

    public class RegisterState {
        public Assembly.Register info;
        public int value;

        @Override
        public String toString() {
            return info.name + " = " + value;
        }
    }

    private interface InstrExecutor {
        /**
         *
         * @param instr
         * @return new cursor or -1 if instruction don't want to jump
         */
        int execute(Instruction instr);
    }

    public MemorySource sourceMemory;
    public MemorySource destMemory;

    /**
     * Defines a position before given instruction is executed.
     */
    public int instrCursor = 0;
    public List<Instruction> instructions;
    public Instruction lastInstruction = null;
    public Stack<Integer> stack = new Stack<Integer>();
    public int stackMaxSize = 4;
    public final RegisterState[] registerStates;
    public int lastComparison = 0;

    public final InstrExecutor[] instructionExecutors;
    public final TreeMap<String, InstrExecutor> instructionExecutorByName = new TreeMap<String, InstrExecutor>() {{
        put("mov", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int src = getValue(instr.args[0]);
                getReg(instr.args[1]).value = src;

                return -1;
            }
        });
        put("add", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int value1 = getValue(instr.args[0]);
                int value2 = getValue(instr.args[1]);
                getReg(instr.args[2]).value = value1 + value2;

                return -1;
            }
        });
        put("sub", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int value1 = getValue(instr.args[0]);
                int value2 = getValue(instr.args[1]);
                getReg(instr.args[2]).value = value1 - value2;

                return -1;
            }
        });
        put("mul", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int value1 = getValue(instr.args[0]);
                int value2 = getValue(instr.args[1]);
                getReg(instr.args[2]).value = value1 * value2;

                return -1;
            }
        });
        put("div", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int value1 = getValue(instr.args[0]);
                int value2 = getValue(instr.args[1]);
                getReg(instr.args[2]).value = value1 / value2;

                return -1;
            }
        });
        put("and", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int value1 = getValue(instr.args[0]);
                int value2 = getValue(instr.args[1]);
                getReg(instr.args[2]).value = value1 & value2;

                return -1;
            }
        });
        put("orl", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int value1 = getValue(instr.args[0]);
                int value2 = getValue(instr.args[1]);
                getReg(instr.args[2]).value = value1 | value2;

                return -1;
            }
        });
        put("xor", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int value1 = getValue(instr.args[0]);
                int value2 = getValue(instr.args[1]);
                getReg(instr.args[2]).value = value1 ^ value2;

                return -1;
            }
        });
        put("inc", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                getReg(instr.args[0]).value += 1;

                return -1;
            }
        });
        put("dec", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                getReg(instr.args[0]).value -= 1;

                return -1;
            }
        });
        put("push", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int value = getValue(instr.args[0]);
                stack.push(value);

                if (stack.size() >= stackMaxSize)
                    stack.setSize(stackMaxSize);

                return -1;
            }
        });
        put("pop", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                RegisterState reg = getReg(instr.args[0]);

                int value = 0;
                if (!stack.isEmpty())
                    value = stack.pop();

                reg.value = value;

                return -1;
            }
        });
        put("top", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                RegisterState reg = getReg(instr.args[0]);

                int value = 0;
                if (!stack.isEmpty())
                    value = stack.peek();

                reg.value = value;

                return -1;
            }
        });
        put("cmp", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int value1 = getValue(instr.args[0]);
                int value2 = getValue(instr.args[1]);

                int cmp = value2 - value1;
                lastComparison = cmp > 0 ? 1 : (cmp < 0 ? -1 : 0);

                return -1;
            }
        });
        put("ldi", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                RegisterState reg = getReg(instr.args[0]);
                reg.value = loadSourceByte();

                return -1;
            }
        });
        put("sti", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int srcByte = getValue(instr.args[0]);
                int destIndex = getValue(instr.args[1]);
                storeDestinationByte(destIndex, srcByte);

                return -1;
            }
        });
        put("transfer", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int destIndex = getValue(instr.args[0]);
                int srcByte = loadSourceByte();
                storeDestinationByte(destIndex, srcByte);

                return -1;
            }
        });
        put("jump", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int relativeOffset = getValue(instr.args[0]);
                return instrCursor + relativeOffset;
            }
        });
        put("je", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int relativeOffset = getValue(instr.args[0]);
                return lastComparison == EQUAL ? instrCursor + relativeOffset : -1;
            }
        });
        put("jne", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int relativeOffset = getValue(instr.args[0]);
                return lastComparison != EQUAL ? instrCursor + relativeOffset : -1;
            }
        });
        put("jg", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int relativeOffset = getValue(instr.args[0]);
                return lastComparison == GREATER_THAN ? instrCursor + relativeOffset : -1;
            }
        });
        put("jl", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int relativeOffset = getValue(instr.args[0]);
                return lastComparison == LESSER_THAN ? instrCursor + relativeOffset : -1;
            }
        });
        put("jge", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int relativeOffset = getValue(instr.args[0]);
                return lastComparison != LESSER_THAN ? instrCursor + relativeOffset : -1;
            }
        });
        put("jle", new InstrExecutor() {
            @Override
            public int execute(Instruction instr) {
                int relativeOffset = getValue(instr.args[0]);
                return lastComparison != GREATER_THAN ? instrCursor + relativeOffset : -1;
            }
        });
    }};


    public Transputer() {
        // initialize registers
        int n = Assembly.registers.length;
        registerStates = new RegisterState[n];

        for (int i = 0; i < n; ++i) {
            Assembly.Register regInfo = Assembly.registers[i];
            RegisterState reg = new RegisterState();
            reg.info = regInfo;
            reg.value = 0;
            registerStates[i] = reg;
        }

        // initialize instructions
        n = Assembly.opdefs.length;
        instructionExecutors = new InstrExecutor[n];

        for (int i = 0; i < n; ++i) {
            OpDefinition opdef = Assembly.opdefs[i];
            instructionExecutors[opdef.id] = instructionExecutorByName.get(opdef.instructionName);
        }

        for (int i = 0; i < n; ++i) {
            if (instructionExecutors[i] == null) {
                OpDefinition opdef = Assembly.opdefs[i];
                throw new Error("instruction `" + opdef.instructionName + "` has not been implemented");
            }
        }
    }


    /**
     * Execute a single instruction.
     */
    public boolean forward() {
        if (instrCursor >= instructions.size() || instrCursor < 0)
            return false;

        Instruction instr = instructions.get(instrCursor);
        InstrExecutor instrExec = instructionExecutors[instr.opdef.id];
        int newInstrCursor = instrExec.execute(instr);

        if (newInstrCursor >= 0)
            instrCursor = newInstrCursor;
        else
            instrCursor += 1;

        lastInstruction = instr;
        return true;
    }

    public void reset() {
        instrCursor = 0;
        for (RegisterState reg : registerStates) {
            reg.value = 0;
        }
        lastComparison = 0;
        stack.clear();
    }

    private int getValue(InstrArg instrArg) {
        int value;

        if (instrArg.argType == OpArgType.Register) {
            int regId = instrArg.constVal;
            RegisterState reg = registerStates[regId];
            value = reg.value;
        }
        else {
            value = instrArg.constVal;
        }

        return value;
    }

    private RegisterState getReg(InstrArg instrArg) {
        if (instrArg.argType != OpArgType.Register)
            throw new Error("expected register address");

        return registerStates[instrArg.constVal];
    }

    private void storeDestinationByte(int destIndex, int srcByte) {
        destMemory.saveByte(destIndex, (byte)srcByte);
    }

    private int loadSourceByte() {
        return sourceMemory.readValue(-1);
    }
}
