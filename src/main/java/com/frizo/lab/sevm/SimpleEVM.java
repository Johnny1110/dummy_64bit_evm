package com.frizo.lab.sevm;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionDispatcher;
import com.frizo.lab.sevm.nums.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleEVM {

    private final EVMContext context;
    private final InstructionDispatcher dispatcher;

    public SimpleEVM(byte[] bytecode, int initialGas) {
        this.context = new EVMContext(bytecode, initialGas);
        this.dispatcher = new InstructionDispatcher();
    }

    public int getGasRemaining() {
        return context.getGasRemaining();
    }

    /**
     * scan the bytecode to find valid jump destinations
     */
    private void preHandle() {
        log.info("[SimpleEVM] Pre-handling bytecode to find valid jump destinations...");
        for (int i = 0; i < context.getCode().length; i++) {
            if (context.getCode()[i] == Opcode.JUMPDEST.getCode()) {
                context.getValidJumpDestIdx().add(i);
            }
        }
    }

    public void run() {
        preHandle();

        while (context.isRunning() && context.hasMoreCode()) {
            Opcode opcode = context.getCurrentOpcode();
            consumeGas(opcode);
            context.advancePC();

            try {
                dispatcher.dispatch(context, opcode);
            } catch (Exception e) {
                context.halt();
                throw e;
            }
        }
    }


    private void consumeGas(Opcode opcode) {
        context.consumeGas(opcode.getGasCost());
        log.info("[SimpleEVM] consumeGas: {} ({} gas), gasRemaining: {}", opcode, opcode.getGasCost(), context.getGasRemaining());
    }

    public int peek() {
        return getStack().peek();
    }

    public void printStack() {
        getStack().printStack();
    }

    public void printMemory() {
        context.getMemory().printMemory();
    }

    public void printStorage() {
        context.getStorage().printStorage();
    }

    Stack<Integer> getStack() {
        return context.getStack();
    }

    public int totalGasUsed() {
        return context.getGasUsed();
    }
}
