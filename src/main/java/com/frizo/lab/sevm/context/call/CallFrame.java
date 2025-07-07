package com.frizo.lab.sevm.context.call;

import com.frizo.lab.sevm.common.Constant;
import com.frizo.lab.sevm.context.EVMComponentFactory;
import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.memory.Memory;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.storage.Storage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
public class CallFrame {

    private final String frameId = UUID.randomUUID().toString(); // Unique identifier for the frame, can be used for debugging

    // 64-bit Stack
    private final Stack<Long> stack;
    private final byte[] code;

    // Call Data
    private final String contractAddress;    // contract Address
    private final String caller;            // caller Address
    private final String origin;            // Txn Origin Address
    private final long value;               // transfer value
    private final byte[] inputData;        // input data
    private final long inputOffset;         // input data offset in memory
    private final long inputSize;           // input data size
    // Call Type
    private final CallType callType;
    // Static Call (read-only)
    private final boolean isStatic;
    @Setter
    private Memory<Long, Long> memory;
    @Setter
    private Storage<Long, Long> storage;
    private int pc;
    private long gasRemaining;
    private long gasUsed;
    private boolean running;
    // Call Result
    private byte[] returnData;
    private long returnOffset;
    private long returnSize;
    private boolean reverted;
    private String revertReason;
    @Setter
    private boolean success;

    @Getter
    private final CallReturnDataBuffer callReturnBuffer = new CallReturnDataBuffer(); // Data buffer returned from the call

    // Logs generated during the call
    private final List<LogEntry> logs = new ArrayList<>();

    public CallFrame(byte[] bytecode, long initialGas, CallData callData) {
        this.contractAddress = callData.getContractAddress();
        this.caller = callData.getCaller();
        this.origin = callData.getOrigin();
        this.value = callData.getValue();
        this.code = bytecode;

        this.inputData = callData.getInputData();
        this.inputOffset = callData.getInputOffset();
        this.inputSize = callData.getInputSize();
        this.gasRemaining = initialGas;
        this.callType = callData.getCallType();
        this.isStatic = callData.isStatic();

        this.stack = EVMComponentFactory.createStack(Constant.MAX_STACK_DEPTH);
        this.memory = EVMComponentFactory.createMemory();
        this.storage = EVMComponentFactory.createStorage();
        this.pc = 0;
        this.gasUsed = 0;
        this.running = true;
        this.success = false;
        this.reverted = false;
        this.returnData = new byte[0];
    }

    public CallFrame(EVMContext parentContext, int jumpAddress, long gasLimit) {
        this.contractAddress = "INTERNAL";
        this.caller = "INTERNAL";
        this.origin = "INTERNAL";
        this.value = parentContext.getCurrentFrame().value;
        this.code = parentContext.getCode();
        this.inputData = new byte[0];
        this.inputOffset = 0;
        this.inputSize = 0;

        this.gasRemaining = gasLimit;
        this.gasUsed = 0;

        this.callType = CallType.INTERNAL;
        this.isStatic = false;

        // share components with parent context
        this.stack = parentContext.getStack();
        this.memory = parentContext.getMemory();
        this.storage = parentContext.getStorage();
        this.pc = jumpAddress;

        this.running = true;
        this.success = false;
        this.reverted = false;
        this.returnData = new byte[0];
    }

    @Override
    public String toString() {
        return String.format("CallFrame{id='%s', contract='%s', caller='%s', pc=%d, gasRemaining=%d, running=%b, success=%b, reverted=%b}",
                frameId, contractAddress, caller, pc, gasRemaining, running, success, reverted);
    }

    public void consumeGas(long amount) {
        if (gasRemaining < amount) {
            log.warn("Out of gas in frame: {}, gas remaining: {}, required: {}",
                    this.frameId, gasRemaining, amount);
            throw new EVMException.OutOfGasException();
        }
        gasRemaining -= amount;
        gasUsed += amount;
    }

    public void refundGas(long gasRemaining) {
        this.gasRemaining += gasRemaining;
        this.gasUsed -= gasRemaining;
        log.info("Refunded {} gas to frame:{}, new gas remaining: {}",
                gasRemaining, this.frameId, this.gasRemaining);
    }

    public void halt() {
        running = false;
        //this.memory.cleanUp();
    }

    public void setReturnData(byte[] data, long offset, long size) {
        this.returnOffset = offset;
        this.returnSize = size;
        this.returnData = data;
    }

    public void setReverted(boolean reverted, String reason) {
        this.reverted = reverted;
        this.revertReason = reason;
    }

    public boolean hasMoreCode() {
        return pc < code.length;
    }

    public Opcode getCurrentOp() {
        if (!hasMoreCode()) {
            return Opcode.UNKNOWN;
        }
        return Opcode.fromByte(code[pc]);
    }

    public void advancePC() {
        pc++;
    }

    public void advancePC(int count) {
        pc += count;
    }

    public void updatePC(int newPC) {
        pc = newPC;
    }

    public void addLog(LogEntry logEntry) {
        logs.add(logEntry);
    }

    public void addLogs(List<LogEntry> logs) {
        if (logs != null && !logs.isEmpty()) {
            this.logs.addAll(logs);
        }
    }

    public void cacheReturn(long returnOffset, long returnSize, byte[] returnData) {
        this.callReturnBuffer.setReturnData(returnData, returnOffset, returnSize);
    }

    public void cacheReverted(String revertReason) {
        this.callReturnBuffer.setReverted(revertReason);
    }
}
