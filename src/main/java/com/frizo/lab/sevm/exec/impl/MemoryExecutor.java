package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        switch (opcode) {
            case MSTORE -> {
                int offset = context.getStack().safePop();
                int value = context.getStack().safePop();
                context.getMemory().put(offset, NumUtils.intTo4Bytes(value));
                log.debug("[MemoryExecutor] MSTORE: offset={}, value={}", offset, value);
            }
            case MLOAD -> {
                int offset = context.getStack().safePop();
                byte[] data = context.getMemory().get(offset);
                int value = NumUtils.bytes4ToInt(data);
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
