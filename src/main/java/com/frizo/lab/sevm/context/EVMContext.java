package com.frizo.lab.sevm.context;

import com.frizo.lab.sevm.common.Constant;
import com.frizo.lab.sevm.context.call.CallData;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.context.call.CallType;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.memory.Memory;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.stack.call.CallStack;
import com.frizo.lab.sevm.storage.Storage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Getter
public class EVMContext {

    // Global context
    protected final String txOrigin;
    protected final long blockNumber;
    protected final long timestamp;
    // call
    private final CallStack callStack;
    private final Set<Integer> validJumpDestIdx;

    public EVMContext(byte[] bytecode, int initialGas, String txOrigin) {
        this.callStack = new CallStack(Constant.MAX_STACK_DEPTH);
        this.validJumpDestIdx = new HashSet<>();

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


    public CallFrame getCurrentFrame() {
        return callStack.peek();
    }

    public Stack<Integer> getStack() {
        return getCurrentFrame().getStack();
    }

    public Memory<Integer, byte[]> getMemory() {
        return getCurrentFrame().getMemory();
    }

    public Storage<Integer, byte[]> getStorage() {
        return getCurrentFrame().getStorage();
    }

    public byte[] getCode() {
        return getCurrentFrame().getCode();
    }


    public int getPc() {
        return getCurrentFrame().getPc();
    }

    public int getGasRemaining() {
        return getCurrentFrame().getGasRemaining();
    }

    public int getGasUsed() {
        return getCurrentFrame().getGasUsed();
    }

    public boolean isRunning() {
        return getCurrentFrame().isRunning();
    }

    public void consumeGas(int amount) {
        getCurrentFrame().consumeGas(amount);
    }

    public void refundGas(int gasRemaining) {
        getCurrentFrame().refundGas(gasRemaining);
    }

    public void halt() {
        getCurrentFrame().halt();
    }

    public void stop() {
        getCurrentFrame().halt();
    }

    public void updatePC(int idx) {
        getCurrentFrame().updatePC(idx);
    }

    public boolean hasMoreCode() {
        return getCurrentFrame().hasMoreCode();
    }

    public Opcode getCurrentOpcode() {
        return getCurrentFrame().getCurrentOp();
    }

    public void advancePC() {
        getCurrentFrame().advancePC();
    }

    public void advancePC(int count) {
        getCurrentFrame().advancePC(count);
    }

    public byte getNextByte() {
        CallFrame frame = getCurrentFrame();
        if (frame.getPc() >= frame.getCode().length) {
            return 0;
        }
        return frame.getCode()[frame.getPc()];
    }

    public byte[] getNextBytes(int count) {
        CallFrame frame = getCurrentFrame();
        byte[] result = new byte[count];
        for (int i = 0; i < count; i++) {
            if (frame.getPc() + i < frame.getCode().length) {
                result[i] = frame.getCode()[frame.getPc() + i];
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    public void revert(String revertReason) {
        log.warn("[EVMContext] Reverting frame: {}, reason: {}", getCurrentFrame().getFrameId(), revertReason);
        // TODO recover state with snapshot.
        //getStorage().revert(snapshot);
    }

    public List<LogEntry> getAllLogs() {
        return getCurrentFrame().getLogs();
    }
}
