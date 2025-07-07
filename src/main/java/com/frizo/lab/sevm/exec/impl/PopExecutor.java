package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[PopExecutor] Executing: {}", opcode);

        // Pop the top item from the stack
        if (context.getStack().isEmpty()) {
            throw new EVMException.StackUnderflowException("Stack underflow: POP requires at least one item on the stack");
        }
        long p = context.getStack().safePop();

        log.info("[PopExecutor] POP:{}, current Stack size: {}", p, context.getStack().size());
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode == Opcode.POP;
    }
}
