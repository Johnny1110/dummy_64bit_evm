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
        byte[] code = context.getCode();
        int pc = context.getPc();

        // PUSH1 is 0x60, PUSH2 is 0x61, etc.
        int pushSize = opcode.getCode() - Opcode.PUSH1.getCode() + 1;

        log.info("Executing PUSH operation: {}, size: {}", opcode, pushSize);
        byte[] pushData = new byte[pushSize];
        System.arraycopy(code, pc, pushData, 0, pushSize);
        // update the program counter in the context
        context.updatePC(pc + pushSize);

        int value = NumUtils.bytesToInt(pushData);
        context.getStack().safePush(value);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.isPush();
    }
}
