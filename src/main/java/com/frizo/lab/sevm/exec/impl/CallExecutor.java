package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;

public class CallExecutor implements InstructionExecutor {
    @Override
    public void execute(EVMContext context, Opcode opcode) {
        // TODO: Implement the CALL instruction execution logic
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        // Check if the opcode is a CALL instruction
        return opcode == Opcode.CALL ||
                opcode == Opcode.CALLCODE ||
                opcode == Opcode.DELEGATECALL ||
                opcode == Opcode.STATICCALL ||
                opcode == Opcode.ICALL
                ;
    }
}
