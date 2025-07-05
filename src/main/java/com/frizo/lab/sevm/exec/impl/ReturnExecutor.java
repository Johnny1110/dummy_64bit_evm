package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReturnExecutor implements InstructionExecutor {
    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[ReturnExecutor] Executing: {}", opcode);

        switch (opcode) {
            case RETURN:
                executeReturn(context);
                break;
            case REVERT:
                executeRevert(context);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported return operation: " + opcode);
        }
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.isReturn();
    }

    private void executeReturn(EVMContext context) {
        Stack<Integer> stack = context.getStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 2) {
            throw new IllegalStateException("Stack underflow: RETURN requires at least 2 items on the stack");
        }

        // RETURN: [offset, size]
        int offset = stack.safePop();
        int size = stack.safePop();

        log.info("[ReturnExecutor] RETURN - offset: {}, size: {}", offset, size);

        // read memory data based on offset and size
        byte[] returnData = readMemoryData(context, offset, size);

        // set return data in the current frame
        currentFrame.setReturnData(returnData, offset, size);
        currentFrame.setSuccess(true);
        currentFrame.halt();

        log.info("[ReturnExecutor] Return data size: {}", returnData.length);
    }

    private void executeRevert(EVMContext context) {
        Stack<Integer> stack = context.getStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 2) {
            throw new IllegalStateException("Stack underflow: REVERT requires at least 2 items on the stack");
        }
        // REVERT: [offset, size]
        int offset = stack.safePop();
        int size = stack.safePop();

        log.info("[ReturnExecutor] REVERT - offset: {}, size: {}", offset, size);

        // read memory data based on offset and size
        byte[] revertData = readMemoryData(context, offset, size);
        String revertReason = new String(revertData);

        // set revert reason in the current frame
        currentFrame.setReverted(true, revertReason);
        currentFrame.setSuccess(false);
        currentFrame.halt();

        log.info("[ReturnExecutor] Revert reason: {}", revertReason);
    }

    private byte[] readMemoryData(EVMContext context, int offset, int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            byte[] memData = context.getMemory().get(offset + i);
            data[i] = (memData != null && memData.length > 0) ? memData[0] : 0;
        }
        return data;
    }
}
