package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.nums.Opcode;

public class StopExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        context.stop();
    }

    @Override
    public boolean canHandle(Opcode opcode) {
       return opcode.equals(Opcode.STOP);
    }
}
