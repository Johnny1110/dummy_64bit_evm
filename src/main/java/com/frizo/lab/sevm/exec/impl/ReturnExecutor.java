package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

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

        log.info("[ReturnExecutor] Return data:{}, size: {}", NumUtils.bytesToHex(returnData) ,returnData.length);
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

    /**
     * Reads a range of data from the memory.
     *
     * @param context EVMContext
     * @param offset  offset in memory to start reading from
     * @param size    size of the data to read
     * @return the data read from memory as a byte array
     */
    private byte[] readMemoryData(EVMContext context, int offset, int size) {
        log.info("[CallExecutor] Reading memory data from offset: {}, size: {}", offset, size);
        context.getMemory().printMemory();

        List<Byte> dataList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            byte[] memData = context.getMemory().get(offset + i);
            if (memData != null) {
                // Add all bytes from this memory location
                for (byte b : memData) {
                    dataList.add(b);
                }
            }
        }

        // Convert List<Byte> to byte[]
        byte[] data = new byte[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            data[i] = dataList.get(i);
        }

        return data;
    }

    /**
     * Writes a range of data to the memory.
     *
     * @param context EVMContext
     * @param offset  offset in memory to start writing to
     * @param data    the data to write to memory
     * @param maxSize maximum size to write to memory
     */
    private void writeMemoryData(EVMContext context, int offset, byte[] data, int maxSize) {
        log.info("[CallExecutor] Writing memory data to offset: {}, size: {}", offset, data.length);

        if (data == null || data.length == 0) {
            log.warn("[CallExecutor] No data to write");
            return;
        }

        // Write each byte to consecutive memory locations
        for (int i = 0; i < data.length; i++) {
            // Create a single-byte array for this memory location
            byte[] singleByte = new byte[]{data[i]};
            context.getMemory().put(offset + i, singleByte);
        }

        log.info("[CallExecutor] Successfully wrote {} bytes to memory starting at offset {}", data.length, offset);
        context.getMemory().printMemory();
    }
}
