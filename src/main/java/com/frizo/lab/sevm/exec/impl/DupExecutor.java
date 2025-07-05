package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.nums.Opcode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DupExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        int depth = opcode.getCode() - Opcode.DUP1.getCode() + 1;
        if (context.getStack().size() < depth) {
            log.error("Stack underflow for DUP operation");
            throw new EVMException.StackUnderflowException();
        }
        Integer value = context.getStack().get(depth - 1);
        context.getStack().safePush(value);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.isDup();
    }
}
