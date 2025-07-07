package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;

public class InvalidExecutor implements InstructionExecutor {


    @Override
    public void execute(EVMContext context, Opcode opcode) {
        // 消耗所有剩餘 gas
        context.consumeGas(context.getGasRemaining());

        // 標記為 revert 並停止執行
        context.getCurrentFrame().setSuccess(false);
        context.getCurrentFrame().setReverted(true, "INVALID opcode executed");
        context.halt();
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode == Opcode.INVALID;
    }
}
