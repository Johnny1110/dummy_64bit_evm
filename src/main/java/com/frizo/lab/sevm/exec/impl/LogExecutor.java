package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {

    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.isLog();
    }
}
