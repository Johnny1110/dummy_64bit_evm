package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.utils.NumUtils;

public class StorageExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        switch (opcode) {
            case SSTORE -> {
                int key = context.getStack().safePop();
                int value = context.getStack().safePop();
                context.getStorage().put(key, NumUtils.intTo4Bytes(value)); // store value in storage
            }

            case SLOAD -> {
                int key = context.getStack().safePop();
                int value = NumUtils.bytes4ToInt(context.getStorage().get(key));
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
