package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.utils.MemoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LogExecutor implements InstructionExecutor {

    /**
     * Example LOG3:
     * stack: [dataOffset, dataSize, topic1, topic2, topic3]
     *
     * @param context EVMContext
     * @param opcode  Opcode to execute
     */
    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[LogExecutor] Executing: {}", opcode);
        int logIndex = getLogIndex(opcode);
        if (logIndex > 4 || logIndex < 0) {
            throw new IllegalArgumentException("Invalid log index: " + logIndex + ". Must be between 0 and 4.");
        }

        Stack<Long> stack = context.getStack();
        CallFrame callFrame = context.getCurrentFrame();

        // 1. Pop memory offset and size
        if (stack.size() < 2 + logIndex) {
            throw new EVMException.StackUnderflowException("Stack underflow: LOG requires at least " + (2 + logIndex) + " items on the stack");
        }

        long dataOffset = stack.safePop();
        long dataSize = stack.safePop();

        // 2. Pop topics from stack (max 4 topics) 1, 2, 3, 4
        List<Long> topics = new ArrayList<>(4);
        for (int i = 0; i < logIndex; i++) {
            topics.add(stack.safePop());
        }

        // 3. Read data from memory
        byte[] data = MemoryUtils.read(context, dataOffset, dataSize);

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

}
