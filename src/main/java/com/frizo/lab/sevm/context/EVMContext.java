package com.frizo.lab.sevm.context;

import com.frizo.lab.sevm.blockchain.Blockchain;
import com.frizo.lab.sevm.blockchain.impl.BlockChainFactory;
import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.common.Constant;
import com.frizo.lab.sevm.context.block.BlockContext;
import com.frizo.lab.sevm.context.call.CallData;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.context.call.CallType;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.context.txn.TxnContext;
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

    private static final long DEFAULT_GAS_LIMIT = 1000000L;

    // Global context
    private final BlockContext blockContext;
    private final TxnContext txnContext;

    // call
    private final CallStack callStack;
    private final Set<Integer> validJumpDestIdx;

    // Blockchain instance for state access (StageDB)
    @Getter
    private final Blockchain blockchain = BlockChainFactory.getMockStateDB();

    public EVMContext(byte[] bytecode, long initialGas, Address txOrigin) {
        this.callStack = new CallStack(Constant.MAX_STACK_DEPTH);
        this.validJumpDestIdx = new HashSet<>();

        Address contractAddress = Address.of("0x0000000000000000");// for test, use a dummy address

        this.blockContext = new BlockContext(blockchain, DEFAULT_GAS_LIMIT);
        this.txnContext = new TxnContext(blockchain, txOrigin);

        // Create the initial call frame
        CallData callData = CallData.builder()
                .contractAddress(contractAddress)
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

    public EVMContext(byte[] bytecode, long value, long initialGas, Address txOrigin) {
        this.callStack = new CallStack(Constant.MAX_STACK_DEPTH);
        this.validJumpDestIdx = new HashSet<>();

        Address contractAddress = Address.of("0x0000000000000000");// for test, use a dummy address

        this.blockContext = new BlockContext(blockchain, DEFAULT_GAS_LIMIT);
        this.txnContext = new TxnContext(blockchain, txOrigin);

        // Create the initial call frame
        CallData callData = CallData.builder()
                .contractAddress(contractAddress)
                .caller(txOrigin)
                .origin(txOrigin)
                .value(value)
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

    public Stack<Long> getCurrentStack() {
        return getCurrentFrame().getStack();
    }

    public Memory<Long, Long> getCurrentMemory() {
        return getCurrentFrame().getMemory();
    }

    public Storage<Long, Long> getStorage() {
        return getCurrentFrame().getStorage();
    }

    public byte[] getCurrentCode() {
        return getCurrentFrame().getCode();
    }


    public int getCurrentPc() {
        return getCurrentFrame().getPc();
    }

    public long getGasRemaining() {
        return getCurrentFrame().getGasRemaining();
    }

    public long getGasUsed() {
        return getCurrentFrame().getGasUsed();
    }

    public boolean isRunning() {
        return getCurrentFrame().isRunning();
    }

    public void consumeGas(long amount) {
        getCurrentFrame().consumeGas(amount);
    }

    public void refundGas(long gasRemaining) {
        getCurrentFrame().refundGas(gasRemaining);
    }

    public void halt() {
        getCurrentFrame().halt();
    }

    public void stop() {
        getCurrentFrame().halt();
    }

    public void updateCurrentPC(int idx) {
        getCurrentFrame().updatePC(idx);
    }

    public boolean hasMoreCode() {
        return getCurrentFrame().hasMoreCode();
    }

    public Opcode getCurrentOpcode() {
        return getCurrentFrame().getCurrentOp();
    }

    public void advanceCurrentPC() {
        getCurrentFrame().advancePC();
    }

    public void advanceCurrentPC(int count) {
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

    public void setByteCode(byte[] code) {
        this.getCurrentFrame().setByteCode(code);
    }

    public void preExecHandle() {
        log.info("[EVMContext] Pre-handling bytecode to find valid jump destinations...");
        for (int i = 0; i < this.getCurrentCode().length; i++) {
            if (this.getCurrentCode()[i] == Opcode.JUMPDEST.getCode()) {
                this.getValidJumpDestIdx().add(i);
            }
        }
    }

    public void setValue(long value) {
        getCurrentFrame().setValue(value);
    }

    public void setContractAddress(Address contractAddress) {
        getCurrentFrame().setContractAddress(contractAddress);
    }

    public void setCallData(byte[] callData) {
        getCurrentFrame().setInputData(callData);
        getCurrentFrame().setInputOffset(0);
        getCurrentFrame().setInputSize(callData.length);
    }

    public void setStaticCall(boolean is) {
        getCurrentFrame().setStatic(is);
    }

    public void creationMode() {
        getCurrentFrame().enableCreationMode();
    }

    public int getDepth() {
        return getCurrentFrame().getStack().size();
    }
}
