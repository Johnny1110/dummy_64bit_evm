package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.utils.MemoryUtils;
import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReturnRevertExecutor implements InstructionExecutor {
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
        Stack<Long> stack = context.getStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 2) {
            throw new IllegalStateException("Stack underflow: RETURN requires at least 2 items on the stack");
        }

        // RETURN: [offset, size]
        long offset = stack.safePop();
        long size = stack.safePop();

        log.info("[ReturnExecutor] RETURN - offset: {}, size: {}", offset, size);

        // read memory data based on offset and size
        byte[] returnData = MemoryUtils.read(context, offset, size);

        // set return data in the current frame
        currentFrame.setReturnData(returnData, offset, size);
        currentFrame.setSuccess(true);
        currentFrame.halt();

        log.info("[ReturnExecutor] Return data:{}, size: {}", NumUtils.bytesToHex(returnData), returnData.length);
    }

    private void executeRevert(EVMContext context) {
        Stack<Long> stack = context.getStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 2) {
            throw new IllegalStateException("Stack underflow: REVERT requires at least 2 items on the stack");
        }
        // REVERT: [offset, size]
        long offset = stack.safePop();
        long size = stack.safePop();

        log.info("[ReturnExecutor] REVERT - offset: {}, size: {}", offset, size);

        // read memory data based on offset and size
        byte[] revertData = MemoryUtils.read(context, offset, size);
        log.info("[ReturnExecutor] REVERT data: {}", NumUtils.bytesToHex(revertData));
        String revertReason = NumUtils.bytesToString(revertData, 8);

        // set revert reason in the current frame
        currentFrame.setReverted(true, revertReason);
        currentFrame.setSuccess(false);
        currentFrame.halt();

        log.info("[ReturnExecutor] Revert reason: {}", revertReason);
    }
}
