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
import com.frizo.lab.sevm.utils.MemoryUtils;
import com.frizo.lab.sevm.utils.NumUtils;
import com.frizo.lab.sevm.vm.SimpleEVM;
import lombok.extern.slf4j.Slf4j;

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
        Stack<Long> stack = context.getCurrentStack();
        CallFrame currentFrame = context.getCurrentFrame();

        // CALL's stack :[gas, address, value, argsOffset, argsSize, retOffset, retSize]
        if (stack.size() < 7) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for CALL");
        }
        long gas = stack.safePop();
        long contractAddress = stack.safePop();
        long value = stack.safePop();
        long argsOffset = stack.safePop();
        long argsSize = stack.safePop();
        long retOffset = stack.safePop();
        long retSize = stack.safePop();

        log.info("[CallExecutor] CALL - gas: {}, contractAddress: {}, value: {}", gas, NumUtils.longToHex(contractAddress), value);

        // read the call data from memory.
        byte[] callData = MemoryUtils.read(context, argsOffset, argsSize);
        log.info("[CallExecutor] CALL - load callData from memory: {}", callData);
        // load the contract code for the given contractAddress.
        byte[] contractCode;
        try {
            contractCode = loadContractCode(context, contractAddress);
        } catch (EVMException ex) {
            // revert.
            context.getCurrentFrame().setSuccess(false);
            context.getCurrentFrame().setReverted(true, ex.getMessage());
            stack.safePush(0L);
            return;
        }

        // Create a new call frame for the called contract
        CallFrame newFrame = new CallFrame(
                contractCode, gas,
                CallData.builder()
                        .contractAddress(NumUtils.longToHex(contractAddress))
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
            //log.info("[CallExecutor] CALL - write CALL contract return data to memory at offset: {}, size: {}", retOffset, newFrame.getReturnData().length);
            //MemoryUtils.write(context, retOffset, newFrame.getReturnData());
        }

        // Push the success status onto the stack
        stack.safePush(success ? 1L : 0);

    }

    /**
     * EVM not define ICALL (0xF8) instruction.
     * ICALL is a custom instruction for internal calls.
     * Internal calls are used to jump to another function within the same contract.
     *
     * @param context
     */
    private void executeInternalCall(EVMContext context) {
        Stack<Long> stack = context.getCurrentStack();

        // ICALL [address, gas]
        if (stack.size() < 2) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for ICALL");
        }
        int jumpPC = Math.toIntExact(stack.safePop());
        long gas = stack.safePop();

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
        Stack<Long> stack = context.getCurrentStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 6) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for STATICCALL");
        }

        // STATICCALL: [gas, address, argsOffset, argsSize, retOffset, retSize]
        long gas = stack.safePop();
        long contractAddress = stack.safePop();
        long argsOffset = stack.safePop();
        long argsSize = stack.safePop();
        long retOffset = stack.safePop();
        long retSize = stack.safePop();

        log.info("[CallExecutor] STATICCALL - gas: {}, contractAddress: {}", gas, contractAddress);

        // STATICCALL read-only
        byte[] callData = MemoryUtils.read(context, argsOffset, argsSize);

        byte[] contractCode;
        try {
            contractCode = loadContractCode(context, contractAddress);
        } catch (EVMException ex) {
            // revert.
            context.getCurrentFrame().setSuccess(false);
            context.getCurrentFrame().setReverted(true, ex.getMessage());
            stack.safePush(0L);
            return;
        }

        CallFrame newFrame = new CallFrame(
                contractCode, gas,
                CallData.builder()
                        .contractAddress(NumUtils.longToHex(contractAddress))
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
            //log.info("[CallExecutor] STATICCALL - write STATICCALL contract return data to memory at offset: {}, size: {}", retOffset, newFrame.getReturnData().length);
            //MemoryUtils.write(context, retOffset, newFrame.getReturnData());
        }

        stack.safePush(success ? 1L : 0);
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
        Stack<Long> stack = context.getCurrentStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 6) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for DELEGATECALL");
        }

        // DELEGATECALL:[gas, address, argsOffset, argsSize, retOffset, retSize]
        long gas = stack.safePop();
        long contractAddress = stack.safePop();
        long argsOffset = stack.safePop();
        long argsSize = stack.safePop();
        long retOffset = stack.safePop();
        long retSize = stack.safePop();

        log.info("[CallExecutor] DELEGATECALL - gas: {}, contractAddress: {}", gas, contractAddress);

        // DELEGATECALL keep all current context （msg.sender, msg.value, storage）
        byte[] callData = MemoryUtils.read(context, argsOffset, argsSize);

        byte[] contractCode;
        try {
            contractCode = loadContractCode(context, contractAddress);
        } catch (EVMException ex) {
            // revert.
            context.getCurrentFrame().setSuccess(false);
            context.getCurrentFrame().setReverted(true, ex.getMessage());
            stack.safePush(0L);
            return;
        }

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
            //log.info("[CallExecutor] DELEGATECALL - write DELEGATECALL contract return data to memory at offset: {}, size: {}", retOffset, newFrame.getReturnData().length);
            //MemoryUtils.write(context, retOffset, newFrame.getReturnData());
        }

        stack.safePush(success ? 1L : 0);
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
        Stack<Long> stack = context.getCurrentStack();
        CallFrame currentFrame = context.getCurrentFrame();

        if (stack.size() < 7) {
            throw new EVMException.StackUnderflowException("Not enough items on stack for CALLCODE");
        }
        // CALLCODE same as CALL
        long gas = stack.safePop();
        long contractAddress = stack.safePop();
        long value = stack.safePop();
        long argsOffset = stack.safePop();
        long argsSize = stack.safePop();
        long retOffset = stack.safePop();
        long retSize = stack.safePop();

        log.info("[CallExecutor] CALLCODE - gas: {}, contractAddress : {}, value: {}", gas, contractAddress, value);

        byte[] callData = MemoryUtils.read(context, argsOffset, argsSize);

        byte[] contractCode;
        try {
            contractCode = loadContractCode(context, contractAddress);
        } catch (EVMException ex) {
            // revert.
            context.getCurrentFrame().setSuccess(false);
            context.getCurrentFrame().setReverted(true, ex.getMessage());
            stack.safePush(0L);
            return;
        }

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
            //log.info("[CallExecutor] CALLCODE - write CALLCODE contract return data to memory at offset: {}, size: {}", retOffset, newFrame.getReturnData().length);
            //MemoryUtils.write(context, retOffset, newFrame.getReturnData());
        }

        stack.safePush(success ? 1L : 0);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        // Check if the opcode is a CALL instruction
        return opcode.isCall();
    }

    /**
     * Loads the contract code for the given address.
     *
     * @param context
     * @param contractAddress contract address
     * @return the contract code as a byte array
     */
    private byte[] loadContractCode(EVMContext context, long contractAddress) {
        log.info("[CallExecutor] Loading contract code for contractAddress: {}", NumUtils.longToHex(contractAddress));
        try {
            return context.getBlockchain().loadCode(NumUtils.longToHex(contractAddress));
        } catch (Exception e) {
            log.error("[CallExecutor] Failed to load contract code for address {}: {}", NumUtils.longToHex(contractAddress), e.getMessage());
            throw new EVMException.ContractNotFoundException("Contract not found at address: " + NumUtils.longToHex(contractAddress));
        }
    }

    /**
     * Executes a call frame in the EVM context.
     * This method simulates the execution of a call frame, pushing it onto the call stack
     *
     * @param context
     * @param frame
     * @return
     */
    private boolean executeCallFrame(EVMContext context, CallFrame frame, long transferGas) {
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

            log.info("<executeFrameCode> --------------- ⚠⚠⚠ Context Content Check before run ⚠⚠⚠ ---------------");
            System.out.println("⚡ Available Gas ⚡: " + context.getGasRemaining());
            context.getCurrentStack().printStack();
            context.getCurrentMemory().printMemory();
            context.getStorage().printStorage();
            log.info("<executeFrameCode> --------------- ⚠⚠⚠ Context Content Check before run ⚠⚠⚠ ---------------");

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
