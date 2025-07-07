package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallDataExecutor implements InstructionExecutor {
    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[CallDataExecutor] Executing Opcode: {}", opcode);
        switch (opcode) {
            case CALLDATALOAD:
                callDataLoad(context);
                break;
            case CALLDATASIZE:
                callDataSize(context);
                break;
            case CALLDATACOPY:
                callDataCopy(context);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
        }
    }

    // memory[destOffset:destOffset+length] = msg.data[offset:offset+length]
    private void callDataCopy(EVMContext context) {
        // TODO: Implement CALLDATACOPY
    }

    // msg.data.size
    private void callDataSize(EVMContext context) {
        // TODO: Implement CALLDATASIZE
    }

    // msg.data[i:i+32]
    // reads a long from message data
    private void callDataLoad(EVMContext context) {
        // TODO: Implement CALLDATALOAD
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.getExecutorClass().equals(CallDataExecutor.class);
    }
}
