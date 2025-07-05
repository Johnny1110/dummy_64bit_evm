package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;

public class ReturnExecutor implements InstructionExecutor {
    @Override
    public void execute(EVMContext context, Opcode opcode) {
        // TODO: Implement the RETURN instruction execution logic
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode == Opcode.RETURN ||
               opcode == Opcode.REVERT;
    }
}
