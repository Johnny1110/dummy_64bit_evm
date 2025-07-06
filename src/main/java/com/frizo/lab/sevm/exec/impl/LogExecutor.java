package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LogExecutor implements InstructionExecutor {

    /**
     *  Example LOG3:
     *  stack: [dataOffset, dataSize, topic1, topic2, topic3]
     * @param context EVMContext
     * @param opcode Opcode to execute
     */
    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[LogExecutor] Executing: {}", opcode);
        int logIndex = getLogIndex(opcode);
        if (logIndex > 4 || logIndex < 0) {
            throw new IllegalArgumentException("Invalid log index: " + logIndex + ". Must be between 0 and 4.");
        }

        Stack<Integer> stack = context.getStack();
        CallFrame callFrame = context.getCurrentFrame();

        // 1. Pop memory offset and size
        if (stack.size() < 2 + logIndex) {
            throw new EVMException.StackUnderflowException("Stack underflow: LOG requires at least " + (2 + logIndex) + " items on the stack");
        }

        int dataOffset = stack.safePop();
        int dataSize = stack.safePop();

        // 2. Pop topics from stack (max 4 topics) 1, 2, 3, 4
        List<Integer> topics = new ArrayList<>(4);
        for (int i = 0; i < logIndex; i++) {
            topics.add(stack.safePop());
        }

        // 3. Read data from memory
        byte[] data = readMemoryData(context, dataOffset, dataSize);

        // 4. Create LogEntry
        LogEntry logEntry = LogEntry.builder()
                .contractAddress(callFrame.getContractAddress())
                .topics(topics)
                .data(data)
                .blockNumber(context.getBlockNumber())
                .txOrigin(context.getTxOrigin())
                .build();

        // 5. Append to current frame logs
        callFrame.addLog(logEntry);

        log.info("[LogExecutor] Emitted log entry: {}", logEntry);
    }

    private int getLogIndex(Opcode opcode) {
        if (!canHandle(opcode)) {
            throw new IllegalArgumentException("Unsupported opcode for LogExecutor: " + opcode);
        }
        // LOG0 (0xA0) -> 0, LOG1 (0xA1) -> 1, ..., LOG4 (0xA4) -> 4
        return opcode.getCode() - Opcode.LOG0.getCode();
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        // process only LOG opcodes
        // LOGn (0xA0 ~ 0xA4)
        return opcode.isLog();
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
        log.info("[LogExecutor] Reading memory data from offset: {}, size: {}", offset, size);

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
     * @param size    maximum size to write to 1 memory address
     */
    private void writeMemoryData(EVMContext context, int offset, byte[] data, int size) {
        log.info("[LogExecutor] Writing memory data to offset: {}, dataSize: {}, fixedSizePerAddress: {}",
                offset, data.length, size);

        if (data.length == 0) {
            log.warn("[LogExecutor] No data to write");
            return;
        }

        if (size <= 0) {
            log.warn("[CallExecutor] Invalid size parameter: {}", size);
            return;
        }

        int dataIndex = 0;
        int currentOffset = offset;

        // Write data in fixed chunks of 'size' bytes per memory address
        while (dataIndex < data.length) {
            // Create fixed-size chunk (pad with zeros if needed)
            byte[] chunk = new byte[size];

            // Copy available data
            int remainingBytes = data.length - dataIndex;
            int bytesToCopy = Math.min(size, remainingBytes);
            System.arraycopy(data, dataIndex, chunk, 0, bytesToCopy);

            // Remaining bytes in chunk are already zero (default value)

            // Write chunk to memory address
            context.getMemory().put(currentOffset, chunk);

            log.debug("[CallExecutor] Wrote {} bytes (padded to {}) to memory address {}",
                    bytesToCopy, size, currentOffset);

            // Move to next memory address and data position
            currentOffset++;
            dataIndex += bytesToCopy;
        }

        int addressesUsed = currentOffset - offset;
        log.info("[CallExecutor] Successfully wrote {} bytes to {} memory addresses starting at offset {}",
                data.length, addressesUsed, offset);
    }
}
