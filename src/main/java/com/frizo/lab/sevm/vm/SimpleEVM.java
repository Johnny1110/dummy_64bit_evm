package com.frizo.lab.sevm.vm;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.exec.InstructionDispatcher;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.utils.NumUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SimpleEVM {

    @Getter
    private final EVMContext context;
    private final InstructionDispatcher dispatcher;

    public SimpleEVM(byte[] bytecode, long initialGas, String originAddr) {
        this.context = new EVMContext(bytecode, initialGas, originAddr);
        this.dispatcher = new InstructionDispatcher();
    }

    // for CallFrame execution
    public SimpleEVM(EVMContext context) {
        this.context = context;
        this.dispatcher = new InstructionDispatcher();
    }

    public long getGasRemaining() {
        return context.getGasRemaining();
    }

    /**
     * scan the bytecode to find valid jump destinations
     */
    private void preHandle() {
        log.info("[SimpleEVM] Pre-handling bytecode to find valid jump destinations...");
        for (int i = 0; i < context.getCurrentCode().length; i++) {
            if (context.getCurrentCode()[i] == Opcode.JUMPDEST.getCode()) {
                context.getValidJumpDestIdx().add(i);
            }
        }
    }

    public void run() {
        log.info("[SimpleEVM] Starting execution, Frame:{}, ...",
                getContext().getCurrentFrame());
        preHandle();

        while (context.isRunning() && context.hasMoreCode()) {
            Opcode opcode = context.getCurrentOpcode();
            consumeGas(opcode);
            context.advanceCurrentPC();

            try {
                dispatcher.dispatch(context, opcode);
            } catch (Exception e) {
                log.error("[SimpleEVM] Error executing frame: {}", getContext().getCurrentFrame(), e);
                context.halt();
                context.getCurrentFrame().setReverted(true, e.getMessage());
                throw e;
            }
        }
    }


    private void consumeGas(Opcode opcode) {
        context.consumeGas(opcode.getGasCost());
        log.info("[SimpleEVM] consumeGas: {} ({} gas), gasRemaining: {}", opcode, opcode.getGasCost(), context.getGasRemaining());
    }

    public long peek() {
        return getStack().peek();
    }

    public void printStack() {
        getStack().printStack();
    }

    public void printMemory() {
        context.getCurrentMemory().printMemory();
    }

    public void printStorage() {
        context.getStorage().printStorage();
    }

    public Stack<Long> getStack() {
        return context.getCurrentStack();
    }

    public long totalGasUsed() {
        return context.getGasUsed();
    }

    public boolean isRunning() {
        return context.isRunning();
    }

    public List<LogEntry> getAllLogs() {
        return context.getAllLogs();
    }

    public void registerContract(byte[] contractAddress, byte[] contractBytecode) {
        String contractAddressHex = NumUtils.bytesToHex(contractAddress);
        context.getBlockchain().registerContract(contractAddressHex, contractBytecode);
    }
}
