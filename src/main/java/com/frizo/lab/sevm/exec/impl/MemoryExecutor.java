package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        switch (opcode) {
            case MSTORE -> {
                // 8 bytes as default size for MSTORE
                long offset = context.getStack().safePop();
                long value = context.getStack().safePop();
                context.getMemory().put(offset, 8, value);
                log.debug("[MemoryExecutor] MSTORE: offset={}, value={}", offset, value);
            }
            case MLOAD -> {
                // 8 bytes as default size for MLOAD
                long offset = context.getStack().safePop();
                long value = context.getMemory().get(offset, 8);
                context.getStack().safePush(value);
                log.debug("[MemoryExecutor] MLOAD: offset={}, value={}", offset, value);
            }
            default -> throw new EVMException.UnknownOpcodeException(opcode);
        }
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return switch (opcode) {
            case MSTORE, MLOAD -> true;
            default -> false;
        };
    }
}
