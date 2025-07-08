package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.utils.MemoryUtils;
import com.frizo.lab.sevm.utils.NumUtils;
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

    // msg.data.size
    private void callDataSize(EVMContext context) {
        long callDataSize = context.getCurrentFrame().getInputSize();
        context.getCurrentFrame().getStack().safePush(callDataSize);
        log.info("[CallDataExecutor] CALLDATASIZE: {}", callDataSize);
    }

    // memory[destOffset:destOffset+length] = msg.data[offset:offset+length]
    private void callDataCopy(EVMContext context) {
        if (context.getCurrentStack().size() < 3) {
            throw new EVMException.StackUnderflowException("CALLDATACOPY requires at least three values on the stack");
        }

        // target memory offset
        long destOffset = context.getCurrentFrame().getStack().safePop();
        // calldata offset
        long offset = context.getCurrentFrame().getStack().safePop();
        // length of data to copy
        long length = context.getCurrentFrame().getStack().safePop();

        // Validate offsets and length
        if (destOffset < 0 || offset < 0 || length < 0) {
            throw new IndexOutOfBoundsException("CALLDATACOPY out of bounds: destOffset=" + destOffset + ", offset=" + offset + ", length=" + length);
        }
        byte[] inputData = context.getCurrentFrame().getInputData();
        byte[] dataToCopy = NumUtils.cutBytes(inputData, offset, length);

        // Copy data from calldata to memory
        MemoryUtils.write(context, destOffset, dataToCopy);
        log.info("[CallDataExecutor] CALLDATACOPY: Copied {} bytes from calldata to memory at offset {}", length, destOffset);
    }

    // msg.data[i:i+32]
    // reads a long from message data
    private void callDataLoad(EVMContext context) {
        if (context.getCurrentStack().isEmpty()) {
            throw new EVMException.StackUnderflowException("CALLDATALOAD requires at least one value on the stack");
        }

        long offset = context.getCurrentFrame().getStack().safePop();
        byte[] inputData = context.getCurrentFrame().getInputData();

        // why 8? because 64-bit stack only supports 8 bytes in 1 stack.
        if (offset < 0 || offset + 8 > inputData.length) {
            throw new IndexOutOfBoundsException("CALLDATALOAD offset out of bounds: " + offset);
        }

        long value = NumUtils.readBytes(inputData, offset, 8);

        context.getCurrentFrame().getStack().safePush(value);
        log.info("[CallDataExecutor] CALLDATALOAD: Loaded value {} at offset {}", value, offset);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.getExecutorClass().equals(CallDataExecutor.class);
    }
}
