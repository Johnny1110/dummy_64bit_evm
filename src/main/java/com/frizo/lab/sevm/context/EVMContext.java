package com.frizo.lab.sevm.context;

import com.frizo.lab.sevm.common.Constant;
import com.frizo.lab.sevm.context.call.CallData;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.context.call.CallType;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.memory.Memory;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.stack.call.CallStack;
import com.frizo.lab.sevm.storage.Storage;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class EVMContext {

    protected final Stack<Integer> stack;
    protected final Memory<Integer, byte[]> memory;
    protected final Storage<Integer, byte[]> storage;

    // call
    protected final CallStack callStack;

    // Global context
    protected final String txOrigin;
    protected final long blockNumber;
    protected final long timestamp;

    protected final byte[] code;
    protected int pc;
    protected int gasRemaining;
    protected int gasUsed;
    protected boolean running;
    protected final Set<Integer> validJumpDestIdx;

    protected byte[] returnData;
    protected int returnOffset;
    protected int returnSize;
    protected boolean reverted;
    protected String revertReason;

    public EVMContext(byte[] bytecode, int initialGas, String txOrigin) {
        this.stack = EVMComponentFactory.createStack(Constant.MAX_STACK_DEPTH);
        this.memory = EVMComponentFactory.createMemory();
        this.storage = EVMComponentFactory.createStorage();
        this.code = bytecode;
        this.pc = 0;
        this.gasRemaining = initialGas;
        this.running = true;
        this.validJumpDestIdx = new HashSet<>();
        this.returnData = new byte[0];
        this.reverted = false;

        this.callStack = new CallStack(Constant.MAX_STACK_DEPTH);

        this.txOrigin = txOrigin;
        this.blockNumber = System.currentTimeMillis() / 1000; // Simulating block number as seconds since epoch
        this.timestamp = System.currentTimeMillis();

        // Create the initial call frame
        CallData callData = CallData.builder()
                .contractAddress("CONTRACT_MAIN")
                .caller(txOrigin)
                .origin(txOrigin)
                .value(0)
                .inputData(new byte[0])
                .inputOffset(0)
                .inputSize(0)
                .callType(CallType.CALL)
                .isStatic(false)
                .build();
        CallFrame initialFrame = new CallFrame(
                bytecode,
                initialGas,
                callData
        );
        callStack.safePush(initialFrame);
    }

    public void consumeGas(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Gas amount must be non-negative");
        }
        if (gasRemaining < amount) {
            throw new EVMException.OutOfGasException();
        }
        gasRemaining -= amount;
        gasUsed += amount;
    }

    public void stop() {
        running = false;
    }

    public void updatePC(int idx) {
        pc = idx;
    }

    public boolean hasMoreCode() {
        return pc < code.length;
    }

    public Opcode getCurrentOpcode() {
        if (!hasMoreCode()) {
            throw new EVMException.NoMoreCodeException();
        }
        return Opcode.fromByte(code[pc]);
    }

    public void advancePC() {
        if (pc >= code.length) {
            throw new EVMException.NoMoreCodeException();
        }
        pc++;
    }

    public void advancePC(int steps) {
        if (steps < 0) {
            throw new IllegalArgumentException("Steps must be non-negative");
        }
        if (pc + steps >= code.length) {
            throw new EVMException.NoMoreCodeException();
        }
        pc += steps;
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

    public byte getNextByte() {
        if (pc >= code.length) {
            return 0;
        }
        return code[pc];
    }

    public byte[] getNextBytes(int count) {
        byte[] result = new byte[count];
        for (int i = 0; i < count; i++) {
            if (pc + i < code.length) {
                result[i] = code[pc + i];
            } else {
                result[i] = 0;
            }
        }
        return result;
    }
}
