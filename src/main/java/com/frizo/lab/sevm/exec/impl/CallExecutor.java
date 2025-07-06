package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.call.CallData;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.context.call.CallType;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.stack.call.CallStack;
import com.frizo.lab.sevm.utils.NumUtils;
import com.frizo.lab.sevm.vm.SimpleEVM;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CallExecutor implements InstructionExecutor {
    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[CallExecutor] Executing: {}", opcode);

        switch (opcode) {
            case CALL:
                executeCall(context);
                break;
            case CALLCODE:
                executeCallCode(context);
                break;
            case DELEGATECALL:
                executeDelegateCall(context);
                break;
            case STATICCALL:
                executeStaticCall(context);
                break;
            case ICALL:
                executeInternalCall(context);
                break;
            default:
                throw new EVMException.UnknownOpcodeException(opcode);
        }
    }

    /**
     * CALL (0xF1)
     * External call to another contract.
     * New Frame (Context) is created for the called contract.
     * New Memory and isolation of Storage.
     * Able to transfer value and execute code.
     * Target contract state can be changed.
     *
     * @param context EVMContext
     */
    private void executeCall(EVMContext context) {
        Stack<Integer> stack = context.getStack();
        CallFrame currentFrame = context.getCurrentFrame();

        // CALL's stack :[gas, address, value, argsOffset, argsSize, retOffset, retSize]
        if (stack.size() < 7) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for CALL");
        }
        int gas = stack.safePop();
        int contractAddress = stack.safePop();
        int value = stack.safePop();
        int argsOffset = stack.safePop();
        int argsSize = stack.safePop();
        int retOffset = stack.safePop();
        int retSize = stack.safePop();

        log.info("[CallExecutor] CALL - gas: {}, contractAddress: {}, value: {}", gas, NumUtils.intToHex(contractAddress), value);

        // read the call data from memory.
        byte[] callData = readMemoryData(context, argsOffset, argsSize);
        log.info("[CallExecutor] CALL - load callData from memory: {}", callData);
        // load the contract code for the given contractAddress.
        byte[] contractCode = loadContractCode(contractAddress);

        // Create a new call frame for the called contract
        CallFrame newFrame = new CallFrame(
                contractCode, gas,
                CallData.builder()
                        .contractAddress(NumUtils.intToHex(contractAddress))
                        .caller(currentFrame.getContractAddress())
                        .origin(currentFrame.getOrigin())
                        .value(value)
                        .inputData(callData)
                        .inputOffset(argsOffset)
                        .inputSize(argsSize)
                        .callType(CallType.CALL)
                        .isStatic(false)
                        .build()
        );

        boolean success = executeCallFrame(context, newFrame, gas);

        if (success && newFrame.getReturnData().length > 0) {
            writeMemoryData(context, retOffset, newFrame.getReturnData(), retSize);
        }

        // Push the success status onto the stack
        stack.safePush(success ? 1 : 0);

    }

    /**
     * EVM not define ICALL (0xF8) instruction.
     * ICALL is a custom instruction for internal calls.
     * Internal calls are used to jump to another function within the same contract.
     *
     * @param context
     */
    private void executeInternalCall(EVMContext context) {
        Stack<Integer> stack = context.getStack();

        // ICALL [address, gas]
        if (stack.size() < 2) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for ICALL");
        }
        int jumpPC = stack.safePop();
        int gas = stack.safePop();

        log.info("[CallExecutor] ICALL - jumpPC: {}, gas: {}", jumpPC, gas);

        // InternalCall: jump to same contract another function.
        // stack, memory, storage, and other context remain the same.
        CallFrame newFrame = new CallFrame(context, jumpPC, gas);

        boolean success = executeCallFrame(context, newFrame, gas);

        // Internal calls do not return data, but we still need to handle the success status.
        if (!success) {
            throw new EVMException.CallInternalException("Internal call failed");
        }
    }

    /**
     * STATICCALL (0xFA)
     * Static (read-only) call to another contract.
     * Disable state changes in the called contract.
     * Not able to transfer value.
     * Ensure not to modify the state of the called contract.
     * Usually used for read-only operations (Query). view pure functions.
     *
     * @param context EVMContext
     */
    private void executeStaticCall(EVMContext context) {
        Stack<Integer> stack = context.getStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 6) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for STATICCALL");
        }

        // STATICCALL: [gas, address, argsOffset, argsSize, retOffset, retSize]
        int gas = stack.safePop();
        int contractAddress = stack.safePop();
        int argsOffset = stack.safePop();
        int argsSize = stack.safePop();
        int retOffset = stack.safePop();
        int retSize = stack.safePop();

        log.info("[CallExecutor] STATICCALL - gas: {}, contractAddress: {}", gas, contractAddress);

        // STATICCALL read-only
        byte[] callData = readMemoryData(context, argsOffset, argsSize);
        byte[] contractCode = loadContractCode(contractAddress);

        CallFrame newFrame = new CallFrame(
                contractCode, gas,
                CallData.builder()
                        .contractAddress(NumUtils.intToHex(contractAddress))
                        .caller(currentFrame.getContractAddress())
                        .origin(currentFrame.getOrigin())
                        .value(0) // STATICCALL does not transfer value
                        .inputData(callData)
                        .inputOffset(argsOffset)
                        .inputSize(argsSize)
                        .callType(CallType.STATICCALL)
                        .isStatic(true) // Static call
                        .build()
        );

        boolean success = executeCallFrame(context, newFrame, gas);

        if (success && newFrame.getReturnData().length > 0) {
            writeMemoryData(context, retOffset, newFrame.getReturnData(), retSize);
        }

        stack.safePush(success ? 1 : 0);
    }

    /**
     * DELEGATECALL (0xF4)
     * All state changes are made in the calling contract's storage.
     * Caller contract share the storage with the called contract.
     * Not support value transfer.
     * Usually used in proxy patterns and library contracts.
     * Must be careful to using DELEGATECALL
     *
     * @param context EVMContext
     */
    private void executeDelegateCall(EVMContext context) {
        Stack<Integer> stack = context.getStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 6) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for DELEGATECALL");
        }

        // DELEGATECALL:[gas, address, argsOffset, argsSize, retOffset, retSize]
        int gas = stack.safePop();
        int contractAddress = stack.safePop();
        int argsOffset = stack.safePop();
        int argsSize = stack.safePop();
        int retOffset = stack.safePop();
        int retSize = stack.safePop();

        log.info("[CallExecutor] DELEGATECALL - gas: {}, contractAddress: {}", gas, contractAddress);

        // DELEGATECALL keep all current context （msg.sender, msg.value, storage）
        byte[] callData = readMemoryData(context, argsOffset, argsSize);
        byte[] contractCode = loadContractCode(contractAddress);

        CallFrame newFrame = new CallFrame(
                contractCode, gas,
                CallData.builder()
                        .contractAddress(currentFrame.getContractAddress())
                        .caller(currentFrame.getCaller())
                        .origin(currentFrame.getOrigin())
                        .value(0) // DELEGATECALL does not transfer value
                        .inputData(callData)
                        .inputOffset(argsOffset)
                        .inputSize(argsSize)
                        .callType(CallType.DELEGATECALL)
                        .isStatic(currentFrame.isStatic())
                        .build());

        // share storage and memory with current frame (DELEGATECALL Critical Feature)
        newFrame.setStorage(currentFrame.getStorage());

        boolean success = executeCallFrame(context, newFrame, gas);

        if (success && newFrame.getReturnData().length > 0) {
            writeMemoryData(context, retOffset, newFrame.getReturnData(), retSize);
        }

        stack.safePush(success ? 1 : 0);
    }

    /**
     * CALLCODE (0xF2)
     * Execute the code of the target contract in the context of the calling contract.
     * Able to transfer value and execute code.
     * State changes are made in the calling contract's storage (pass caller's contract storage to new Frame).
     * CALLCODE was deprecated in Solidity 0.5.0 and replaced by DELEGATECALL (EIP-7).
     */
    @Deprecated(since = "CALLCODE was deprecated in Solidity 0.5.0. EIP-2488")
    private void executeCallCode(EVMContext context) {
        Stack<Integer> stack = context.getStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 7) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for CALLCODE");
        }
        // CALLCODE same as CALL
        int gas = stack.safePop();
        int contractAddress = stack.safePop();
        int value = stack.safePop();
        int argsOffset = stack.safePop();
        int argsSize = stack.safePop();
        int retOffset = stack.safePop();
        int retSize = stack.safePop();

        log.info("[CallExecutor] CALLCODE - gas: {}, contractAddress : {}, value: {}", gas, contractAddress, value);

        byte[] callData = readMemoryData(context, argsOffset, argsSize);
        byte[] contractCode = loadContractCode(contractAddress);

        // create new Frame, but keep current contract address
        CallFrame newFrame = new CallFrame(
                contractCode, gas,
                CallData.builder()
                        .contractAddress(currentFrame.getContractAddress()) // CALLCODE: keep current contract address
                        .caller(currentFrame.getCaller())
                        .origin(currentFrame.getOrigin())
                        .value(value)
                        .inputData(callData)
                        .inputOffset(argsOffset)
                        .inputSize(argsSize)
                        .callType(CallType.CALLCODE)
                        .isStatic(false)
                        .build());

        // share storage（CALLCODE Critical Feature）
        // CALLCODE does not change the storage, it uses the current contract's storage
        newFrame.setStorage(currentFrame.getStorage());

        boolean success = executeCallFrame(context, newFrame, gas);

        if (success && newFrame.getReturnData().length > 0) {
            writeMemoryData(context, retOffset, newFrame.getReturnData(), retSize);
        }

        stack.safePush(success ? 1 : 0);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        // Check if the opcode is a CALL instruction
        return opcode.isCall();
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
        log.info("[CallExecutor] Writing memory data to offset: {}, dataSize: {}, fixedSizePerAddress: {}",
                offset, data.length, size);

        if (data == null || data.length == 0) {
            log.warn("[CallExecutor] No data to write");
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

    /**
     * Loads the contract code for the given address.
     *
     * @param contractAddress contract address
     * @return the contract code as a byte array
     */
    private byte[] loadContractCode(int contractAddress) {
        log.info("[CallExecutor] Loading contract code for contractAddress: {}", NumUtils.intToHex(contractAddress));

        // TODO: Mock a simple contract code for demonstration purposes,
        // TODO: Real implementation should fetch from a blockchain.
        // TODO: This is a simple contract that returns the value 42 when called.
        return new byte[]{
                Opcode.PUSH1.getCode(), (byte) 0xAA,  // PUSH1 170
                Opcode.PUSH1.getCode(), 0x10,  // PUSH1 16 (memory offset)
                Opcode.MSTORE.getCode(),        // MSTORE
                Opcode.PUSH1.getCode(), 0x01,  // PUSH1 1 (return size)
                Opcode.PUSH1.getCode(), 0x10,  // PUSH1 10 (return offset)
                Opcode.RETURN.getCode()         // RETURN
        };
    }

    /**
     * Executes a call frame in the EVM context.
     * This method simulates the execution of a call frame, pushing it onto the call stack
     *
     * @param context
     * @param frame
     * @return
     */
    private boolean executeCallFrame(EVMContext context, CallFrame frame, int transferGas) {
        try {

            // transfer gas to the frame
            log.info("[CallExecutor] Transferring {} gas to new frame: {}", transferGas, frame.getFrameId());
            context.consumeGas(transferGas);

            CallStack callStack = context.getCallStack();

            // ----------------------------------------------------->>>
            // Push the frame onto the call stack
            callStack.safePush(frame);
            log.info("[CallExecutor] Starting execution of frame contract address: {}", frame.getContractAddress());
            // this func won't throw an exception, but will return false if execution fails
            boolean success = executeFrameCode(context, frame);
            callStack.safePop();
            // <<-----------------------------------------------------

            // refund the remaining gas to the frame
            context.refundGas(frame.getGasRemaining());

            log.info("[CallExecutor] Frame execution completed. Success: {}, Reverted: {}",
                    success, frame.isReverted());

            // process reverted frame
            if (frame.isReverted()) {
                log.warn("[CallExecutor] Frame execution reverted: {}", frame.getRevertReason());
                // If the frame is reverted, we can handle it here (e.g., log, revert state changes, etc.)
                // For now, we just return false to indicate failure.
                context.revert(frame.getRevertReason());
                return false;
            }

            return success;
        } catch (Exception e) {
            log.error("[CallExecutor] Call execution failed: {}", e.getMessage());

            // make sure to pop the frame from the call stack
            if (!context.getCallStack().isEmpty()) {
                try {
                    context.getCallStack().safePop();
                } catch (Exception popException) {
                    log.warn("[CallExecutor] Failed to pop frame during error handling");
                }
            }

            return false;
        }
    }

    private boolean executeFrameCode(EVMContext context, CallFrame frame) {
        log.info("[CallExecutor] Executing contract - {} bytecode for frame: {}", frame.getContractAddress(), frame.getFrameId());

        try {

            log.info("<executeFrameCode> --------------- Context Content Check before run ---------------");
            context.getStack().printStack();
            context.getMemory().printMemory();
            context.getStorage().printStorage();
            System.out.println("Available Gas: " + context.getGasRemaining());
            log.info("<executeFrameCode> --------------- Context Content Check before run ---------------");

            SimpleEVM evm = new SimpleEVM(context);
            evm.run();

            log.info("[CallExecutor] Frame execution finished. Gas used: {}, PC: {}",
                    frame.getGasUsed(), frame.getFrameId());

            // if the frame is still running and has no more code to execute,
            if (frame.isRunning() && !frame.hasMoreCode()) {
                frame.setSuccess(true);
                frame.halt();
            }

            return frame.isSuccess();

        } catch (EVMException.OutOfGasException e) {
            log.warn("[CallExecutor] {}", e.getMessage());
            frame.setReverted(true, "Out of gas");
            return false;
        } catch (EVMException.InvalidJumpException e) {
            log.warn("[CallExecutor] {}", e.getMessage());
            frame.setReverted(true, "Invalid jump destination");
            return false;
        } catch (EVMException.StackUnderflowException e) {
            log.warn("[CallExecutor] {}", e.getMessage());
            frame.setReverted(true, "Stack underflow");
            return false;
        } catch (Exception e) {
            log.error("[CallExecutor] {}", e.getMessage());
            frame.setReverted(true, "Execution error: " + e.getMessage());
            return false;
        }
    }
}
