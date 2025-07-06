package com.frizo.lab.sevm.exec;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.op.Opcode;

public interface InstructionExecutor {
    void execute(EVMContext context, Opcode opcode);

    boolean canHandle(Opcode opcode);
}
