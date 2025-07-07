package com.frizo.lab.sevm.exec;

import com.frizo.lab.sevm.blockchain.impl.MockBlockChain;
import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.impl.*;
import com.frizo.lab.sevm.op.Opcode;

import java.util.HashMap;
import java.util.Map;

public class InstructionDispatcher {

    private final Map<Class<? extends InstructionExecutor>, InstructionExecutor> executors;


    public InstructionDispatcher() {
        this.executors = new HashMap<>();
        // Register all instruction executors
        this.executors.put(StopExecutor.class, new StopExecutor());
        this.executors.put(ArithmeticExecutor.class, new ArithmeticExecutor());
        this.executors.put(MemoryExecutor.class, new MemoryExecutor());
        this.executors.put(JumpExecutor.class, new JumpExecutor());
        this.executors.put(DupExecutor.class, new DupExecutor());
        this.executors.put(PushExecutor.class, new PushExecutor());
        this.executors.put(StorageExecutor.class, new StorageExecutor());
        this.executors.put(SwapExecutor.class, new SwapExecutor());
        this.executors.put(CallExecutor.class, new CallExecutor(new MockBlockChain()));
        this.executors.put(PopExecutor.class, new PopExecutor());
        this.executors.put(ReturnRevertExecutor.class, new ReturnRevertExecutor());
        this.executors.put(LogExecutor.class, new LogExecutor());
        this.executors.put(NumLogicInstruction.class, new NumLogicInstruction());
        this.executors.put(PrintExecutor.class, new PrintExecutor());
        this.executors.put(ReturnDataExecutor.class, new ReturnDataExecutor());
    }

    public void dispatch(EVMContext context, Opcode opcode) {
        InstructionExecutor executor = findExecutor(opcode);
        executor.execute(context, opcode);
    }

    private InstructionExecutor findExecutor(Opcode opcode) {
        InstructionExecutor executor = this.executors.get(opcode.getExecutorClass());
        if (executor == null) {
            throw new UnsupportedOperationException("No executor found for opcode: " + opcode);
        }

        if (!executor.canHandle(opcode)) {
            throw new UnsupportedOperationException("Executor " + executor.getClass().getSimpleName() + " cannot handle opcode: " + opcode);
        }

        return executor;
    }

}
