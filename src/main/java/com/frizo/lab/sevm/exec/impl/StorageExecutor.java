package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;

public class StorageExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        switch (opcode) {
            case SSTORE -> {
                // Default storage size is 8 bytes
                long offset = context.getStack().safePop();
                long value = context.getStack().safePop();
                context.getStorage().put(offset, 8, value);
            }

            case SLOAD -> {
                // Default storage size is 8 bytes
                long offset = context.getStack().safePop();
                long value = context.getStorage().get(offset, 8);
                context.getStack().safePush(value);
            }
        }
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return switch (opcode) {
            case SSTORE, SLOAD -> true;
            default -> false;
        };
    }
}
