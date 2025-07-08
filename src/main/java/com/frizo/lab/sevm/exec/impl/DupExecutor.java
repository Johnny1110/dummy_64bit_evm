package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DupExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        int depth = opcode.getCode() - Opcode.DUP1.getCode() + 1;
        if (context.getCurrentStack().size() < depth) {
            log.error("Stack underflow for DUP operation");
            throw new EVMException.StackUnderflowException();
        }
        Long value = context.getCurrentStack().get(depth - 1);
        context.getCurrentStack().safePush(value);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.isDup();
    }
}
