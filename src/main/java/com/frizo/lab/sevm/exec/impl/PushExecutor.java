package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PushExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        // PUSH1 is 0x60, PUSH2 is 0x61, etc.
        int pushSize = opcode.getCode() - Opcode.PUSH1.getCode() + 1;
        byte[] data = context.getNextBytes(pushSize);


        int value = NumUtils.bytesToInt(data);
        context.getStack().safePush(value);
        context.advancePC(pushSize);
        log.info("[PushExecutor] Executing: {}, size: {}, value: {}", opcode, pushSize, value);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.isPush();
    }
}
