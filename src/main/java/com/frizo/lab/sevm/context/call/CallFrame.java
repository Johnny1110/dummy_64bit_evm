package com.frizo.lab.sevm.context.call;

import com.frizo.lab.sevm.common.Constant;
import com.frizo.lab.sevm.context.EVMComponentFactory;
import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.memory.Memory;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.storage.Storage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Getter
public class CallFrame {

    private final String frameId = UUID.randomUUID().toString(); // Unique identifier for the frame, can be used for debugging

    private final Stack<Integer> stack;
    @Setter
    private Memory<Integer, byte[]> memory;
    @Setter
    private Storage<Integer, byte[]> storage;
    private final byte[] code;
    private int pc;
    private int gasRemaining;
    private int gasUsed;
    private boolean running;

    // Call Data
    private final String contractAddress;    // contract Address
    private final String caller;            // caller Address
    private final String origin;            // Txn Origin Address
    private final int value;               // transfer value
    private final byte[] inputData;        // input data
    private final int inputOffset;         // input data offset in memory
    private final int inputSize;           // input data size

    // Call Result
    private byte[] returnData;
    private int returnOffset;
    private int returnSize;
    private boolean reverted;
    private String revertReason;

    @Setter
    private boolean success;

    // Call Type
    private final CallType callType;

    // Static Call (read-only)
    private final boolean isStatic;

    @Override
    public String toString() {
        return String.format("CallFrame{id='%s', contract='%s', caller='%s', pc=%d, gasRemaining=%d, running=%b, success=%b, reverted=%b}",
                frameId, contractAddress, caller, pc, gasRemaining, running, success, reverted);
    }

    public CallFrame(byte[] bytecode, int initialGas, CallData callData) {
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

    public CallFrame(EVMContext parentContext, int jumpAddress, int gasLimit) {
        this.contractAddress = "INTERNAL";
        this.caller = "INTERNAL";
        this.origin = "INTERNAL";
        this.value = 0;
        this.code = parentContext.getCode();
        this.inputData = new byte[0];
        this.inputOffset = 0;
        this.inputSize = 0;
        this.gasRemaining = gasLimit;
        this.callType = CallType.INTERNAL;
        this.isStatic = false;

        // share components with parent context
        this.stack = parentContext.getStack();
        this.memory = parentContext.getMemory();
        this.storage = parentContext.getStorage();
        this.pc = jumpAddress;
        this.gasUsed = 0;
        this.running = true;
        this.success = false;
        this.reverted = false;
        this.returnData = new byte[0];
    }

    public void consumeGas(int amount) {
        if (gasRemaining < amount) {
            throw new EVMException.OutOfGasException();
        }
        gasRemaining -= amount;
        gasUsed += amount;
    }

    public void halt() {
        running = false;
    }

    public void setReturnData(byte[] data, int offset, int size) {
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

}
