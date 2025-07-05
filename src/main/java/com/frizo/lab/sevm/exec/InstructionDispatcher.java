package com.frizo.lab.sevm.exec;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.impl.*;
import com.frizo.lab.sevm.nums.Opcode;

import java.util.Map;

public class InstructionDispatcher {

    private final Map<Class<? extends InstructionExecutor>, InstructionExecutor> executors;


    public InstructionDispatcher() {
        this.executors = Map.of(
                StopExecutor.class, new StopExecutor(),
                ArithmeticExecutor.class, new ArithmeticExecutor(),
                MemoryExecutor.class, new MemoryExecutor(),
                JumpExecutor.class, new JumpExecutor(),
                DupExecutor.class, new DupExecutor(),
                PushExecutor.class, new PushExecutor(),
                StorageExecutor.class, new StorageExecutor(),
                SwapExecutor.class, new SwapExecutor()
        );
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
