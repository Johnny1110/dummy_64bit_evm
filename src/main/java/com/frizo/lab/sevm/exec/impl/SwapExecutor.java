package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SwapExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        int depth = opcode.getCode() - Opcode.SWAP1.getCode() + 1;
        // swap operation requires at least depth + 1 items on the stack
        if (context.getCurrentStack().size() < depth + 1) {
            log.error("Stack underflow for SWAP operation: required {}, but got {}", depth + 1, context.getCurrentStack().size());
            throw new EVMException.StackUnderflowException();
        }

        context.getCurrentStack().swap(0, depth);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.isSwap();
    }
}
