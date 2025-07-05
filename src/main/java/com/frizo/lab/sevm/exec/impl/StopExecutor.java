package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;

public class StopExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        if (context.isRunning()) {
            context.getCurrentFrame().setSuccess(true);
            context.stop();
        }
    }

    @Override
    public boolean canHandle(Opcode opcode) {
       return opcode.equals(Opcode.STOP);
    }
}
