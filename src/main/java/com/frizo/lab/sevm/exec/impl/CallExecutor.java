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

        boolean success = executeCallFrame(context, newFrame);

        if (success && newFrame.getReturnData().length > 0) {
            writeMemoryData(context, retOffset, newFrame.getReturnData(), retSize);
        }

        // Push the success status onto the stack
        stack.safePush(success ? 1 : 0);

    }

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

        boolean success = executeCallFrame(context, newFrame);

        // Internal calls do not return data, but we still need to handle the success status.
        if (!success) {
            throw new EVMException.CallInternalException("Internal call failed");
        }
    }

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

        boolean success = executeCallFrame(context, newFrame);

        if (success && newFrame.getReturnData().length > 0) {
            writeMemoryData(context, retOffset, newFrame.getReturnData(), retSize);
        }

        stack.safePush(success ? 1 : 0);
    }

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
                        .value(currentFrame.getValue())
                        .inputData(callData)
                        .inputOffset(argsOffset)
                        .inputSize(argsSize)
                        .callType(CallType.DELEGATECALL)
                        .isStatic(currentFrame.isStatic())
                        .build());

        // share storage and memory with current frame
        newFrame.setStorage(currentFrame.getStorage());
        //newFrame.setMemory(currentFrame.getMemory());

        boolean success = executeCallFrame(context, newFrame);

        if (success && newFrame.getReturnData().length > 0) {
            writeMemoryData(context, retOffset, newFrame.getReturnData(), retSize);
        }

        stack.safePush(success ? 1 : 0);
    }

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

        // CALLCODE 在當前合約上下文中執行目標合約的程式碼
        byte[] callData = readMemoryData(context, argsOffset, argsSize);
        byte[] contractCode = loadContractCode(contractAddress);

        // create new Frame, but keep current contract address
        CallFrame newFrame = new CallFrame(
                contractCode, gas,
                CallData.builder()
                        .contractAddress(currentFrame.getContractAddress()) // keep current contract address
                        .caller(currentFrame.getCaller())
                        .origin(currentFrame.getOrigin())
                        .value(value)
                        .inputData(callData)
                        .inputOffset(argsOffset)
                        .inputSize(argsSize)
                        .callType(CallType.CALLCODE)
                        .isStatic(false)
                        .build());

        // share storage（這是 CALLCODE 的關鍵特徵）
        newFrame.setStorage(currentFrame.getStorage());

        boolean success = executeCallFrame(context, newFrame);

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
                Opcode.PUSH1.getCode(), (byte)0xAA,  // PUSH1 170
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
    private boolean executeCallFrame(EVMContext context, CallFrame frame) {
        try {
            CallStack callStack = context.getCallStack();
            callStack.safePush(frame);

            log.info("[CallExecutor] Starting execution of frame contract address: {}", frame.getContractAddress());

            boolean success = executeFrameCode(context, frame);

            callStack.safePop();

            log.info("[CallExecutor] Frame execution completed. Success: {}, Reverted: {}",
                    success, frame.isReverted());

            return success && !frame.isReverted();

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
            log.warn("[CallExecutor] Out of gas: {}", e.getMessage());
            frame.setReverted(true, "Out of gas");
            return false;
        } catch (EVMException.InvalidJumpException e) {
            log.warn("[CallExecutor] Invalid jump destination: {}", e.getMessage());
            frame.setReverted(true, "Invalid jump destination");
            return false;
        } catch (EVMException.StackUnderflowException e) {
            log.warn("[CallExecutor] Stack underflow: {}", e.getMessage());
            frame.setReverted(true, "Stack underflow");
            return false;
        } catch (Exception e) {
            log.error("[CallExecutor] Unexpected error during execution: {}", e.getMessage());
            frame.setReverted(true, "Execution error: " + e.getMessage());
            return false;
        }
    }
}
