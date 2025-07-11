package com.frizo.lab.sevm.vm;

import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.common.Constant;
import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionDispatcher;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SEVM {

    @Getter
    private EVMContext context;
    private final InstructionDispatcher dispatcher;

    public SEVM(EVMContext context) {
        if (context == null) {
            throw new IllegalArgumentException("EVMContext cannot be null");
        }
        this.context = context;
        this.dispatcher = new InstructionDispatcher();
    }

    // ------------------------------------------------------------------------------------>

    public EVMResult create(Address caller, Address creationAddress, byte[] initCode, long value, long gasLimit) {
        log.info("[SEVM] Executing contract creation from: {}", caller);
        if (context.getCallDepth() > Constant.MAX_CALL_DEPTH) {
            return EVMResult.failed(new EVMException.CallStackOverflowException(), context);
        }

        // check balance
        if (!context.getBlockchain().canTransfer(caller, value)) {
            throw new EVMException.ErrInsufficientBalance(caller);
        }

        // 3. check exist
        if (context.getBlockchain().exist(creationAddress) ||
                context.getBlockchain().getNonce(creationAddress) != 0 ||
                context.getBlockchain().getCodeHash(creationAddress) != null) {
            log.error("[SEVM] Contract already exists at address: {}", creationAddress);
            return EVMResult.failed(new EVMException.ContractAlreadyExistsException(
                    "Contract already exists at address: " + creationAddress), context);
        }

        // 4. take a snapshot of the current state
        long snapshot = context.getBlockchain().takeSnapshot();

        try {
            // 5. update caller's nonce
            context.getBlockchain().setNonce(caller, context.getBlockchain().getNonce(caller) + 1);

            // 6. create contract address
            context.getBlockchain().createContract(creationAddress);

            // 7. setup nonce for address 1（EIP-161）
            context.getBlockchain().setNonce(creationAddress, 1);

            // 8. transfer value from caller to contract address
            context.getBlockchain().transfer(caller, creationAddress, value);

            // 9. set up context for contract creation
            context.creationMode();
            context.setValue(value);
            context.setContractAddress(creationAddress);
            context.setByteCode(initCode);
            context.setGasLimit(gasLimit);

            // 10. init contract (constructor)
            EVMResult result = executeInternal();

            // 11. process result
            if (result.isSuccess() && result.getReturnData() != null) {
                byte[] deployedCode = result.getReturnData();

                // 12. check deployed code size
                if (deployedCode.length > Constant.MAX_CODE_SIZE) {
                    context.getBlockchain().revertToSnapshot(snapshot);
                    return EVMResult.failed(new EVMException.ErrMaxCodeSizeExceeded(deployedCode.length), context);
                }

                // 13. consume gas for code storage
                long codeStoreGas = (long) deployedCode.length * Constant.CREATE_DATA_GAS;
                if (result.getGasRemaining() < codeStoreGas) {
                    context.getBlockchain().revertToSnapshot(snapshot);
                    return EVMResult.failed(new EVMException.ErrCodeStoreOutOfGas(), context);
                }
                context.consumeGas(codeStoreGas);
                log.info("[SEVM] consumeGas for code storage: {} ({} gas), gasRemaining: {}",
                        deployedCode.length, codeStoreGas, context.getGasRemaining());

                // 14. store deployed code in blockchain state
                context.getBlockchain().setCode(creationAddress, deployedCode);

                log.info("[SEVM] Contract created successfully at address: {}", creationAddress);
                return EVMResult.created(context, creationAddress);
            } else if (result.isReverted()) {
                context.getBlockchain().revertToSnapshot(snapshot);
                return result;
            } else {
                context.getBlockchain().revertToSnapshot(snapshot);
                return result;
            }

        } catch (Exception e) {
            context.getBlockchain().revertToSnapshot(snapshot);
            log.error("[SEVM] Contract creation failed", e);
            return EVMResult.failed(new EVMException.ErrExecutionReverted(e.getMessage()), context);
        }
    }

    // Call executes the contract associated with the addr with the given input as
    // parameters. It also handles any necessary value transfer required and takse
    // the necessary steps to create accounts and reverses the state in case of an
    // execution error or failed value transfer.
    public EVMResult call(Address caller, Address addr, byte[] input, long gasLimit, long value) {
        // Fail if we're trying to execute above the call depth limit
        if (context.getCallDepth() > Constant.MAX_CALL_DEPTH) {
            log.error("[SEVM] Call depth exceeded: {}", Constant.MAX_CALL_DEPTH);
            return EVMResult.failed(new EVMException.CallStackOverflowException(), context);
        }
        log.info("[SEVM] Executing call from: {} to: {}", caller, addr);
        // Fail if we're trying to transfer more than the available balance
        if (value > 0 && !context.getBlockchain().canTransfer(caller, value)) {
            log.error("[SEVM] Insufficient balance for transfer from: {} to: {}", caller, addr);
            return EVMResult.failed(new EVMException.ErrInsufficientBalance(caller), context);
        }

        // snapshot the current state
        long snapshot = context.getBlockchain().takeSnapshot();

        // do transfer if value > 0
        if (value > 0) {
            try {
                context.getBlockchain().transfer(caller, addr, value);
            } catch (EVMException.ValueTransferException e) {
                log.error("[SEVM] Value transfer failed from: {} to: {}", caller, addr, e);
                context.getBlockchain().revertToSnapshot(snapshot);
                return EVMResult.failed(e, context);
            }
        }

        // get contract bytecode
        byte[] contractCode;
        try {
            contractCode = context.getBlockchain().loadCode(addr);
            if (contractCode == null || contractCode.length == 0) {
                throw new EVMException.ContractNotFoundException("Contract not found: " + addr);
            }
        } catch (EVMException.ContractNotFoundException e) {
            log.error("[SEVM] Contract not found: {}", addr, e);
            context.getBlockchain().revertToSnapshot(snapshot);
            return EVMResult.failed(e, context);
        }

        // Set up context for contract call
        context.setByteCode(contractCode);
        context.setCallData(input);
        context.setContractAddress(addr);
        context.setValue(value);
        context.setGasLimit(gasLimit);

        EVMResult result = executeInternal();

        if (result.isSuccess()) {
            log.info("[SEVM] Call executed successfully from: {} to: {}", caller, addr);
            return result;
        } else {
            // If execution failed, revert to the snapshot
            context.getBlockchain().revertToSnapshot(snapshot);
            log.error("[SEVM] Call execution failed from: {} to: {}, reason: {}", caller, addr, result.getMsg());
            return result;
        }
    }

    public EVMResult callCode(Address caller, Address addr, byte[] input, long gasLimit, long value) {
        // check call depth
        if (context.getCallDepth() > Constant.MAX_CALL_DEPTH) {
            log.error("[SEVM] Call depth exceeded: {}", Constant.MAX_CALL_DEPTH);
            return EVMResult.failed(new EVMException.CallStackOverflowException(), context);
        }

        if (value > 0 && !context.getBlockchain().canTransfer(caller, value)) {
            log.error("[SEVM] Insufficient balance for transfer from: {} to: {}", caller, addr);
            return EVMResult.failed(new EVMException.ErrInsufficientBalance(caller), context);
        }

        //get contract bytecode
        byte[] contractCode;
        try {
            contractCode = context.getBlockchain().loadCode(addr);
            if (contractCode == null || contractCode.length == 0) {
                throw new EVMException.ContractNotFoundException("Contract not found: " + addr);
            }
        } catch (EVMException.ContractNotFoundException e) {
            log.error("[SEVM] Contract not found: {}", addr, e);
            return EVMResult.failed(e, context);
        }

        // do snapshot
        long snapshot = context.getBlockchain().takeSnapshot();

        // Set up context for call code
        context.setByteCode(contractCode);
        context.setCallData(input);
        context.setContractAddress(addr);
        context.setValue(value);
        context.setCaller(caller);
        context.setGasLimit(gasLimit);

        EVMResult result = executeInternal();

        if (result.isSuccess()) {
            log.info("[SEVM] Call code executed successfully from: {} to: {}", caller, addr);
            return result;
        } else {
            // If execution failed, revert to the snapshot
            context.getBlockchain().revertToSnapshot(snapshot);
            log.error("[SEVM] Call code execution failed from: {} to: {}, reason: {}", caller, addr, result.getMsg());
            return result;
        }
    }

    public EVMResult delegateCall(Address originCaller, Address caller, Address addr, byte[] input, long gasLimit, long value) {
        // check call depth
        if (context.getCallDepth() > Constant.MAX_CALL_DEPTH) {
            log.error("[SEVM] Call depth exceeded: {}", Constant.MAX_CALL_DEPTH);
            return EVMResult.failed(new EVMException.CallStackOverflowException(), context);
        }

        //get contract bytecode
        byte[] contractCode;
        try {
            contractCode = context.getBlockchain().loadCode(addr);
            if (contractCode == null || contractCode.length == 0) {
                throw new EVMException.ContractNotFoundException("Contract not found: " + addr);
            }
        } catch (EVMException.ContractNotFoundException e) {
            log.error("[SEVM] Contract not found: {}", addr, e);
            return EVMResult.failed(e, context);
        }

        // do snapshot
        long snapshot = context.getBlockchain().takeSnapshot();

        log.info("[SEVM] Executing delegate call from: {} to: {}", originCaller, addr);

        // Set up context for delegate call
        context.setByteCode(contractCode);
        context.setCallData(input);
        context.setContractAddress(addr);
        context.setValue(value);
        context.setCaller(caller);
        context.setOriginCaller(originCaller);
        context.setGasLimit(gasLimit);

        EVMResult result = executeInternal();

        if (result.isSuccess()) {
            log.info("[SEVM] Delegate call executed successfully from: {} to: {}", originCaller, addr);
            return result;
        } else {
            // If execution failed, revert to the snapshot
            context.getBlockchain().revertToSnapshot(snapshot);
            log.error("[SEVM] Delegate call execution failed from: {} to: {}, reason: {}", originCaller, addr, result.getMsg());
            return result;
        }
    }

    /**
     * Execute a static call (read-only)
     */
    public EVMResult staticCall(Address from, Address to, byte[] callData, long gasLimit) {
        log.info("[SEVM] Executing static call from: {} to: {}", from, to);

        // Fail if we're trying to execute above the call depth limit
        if (context.getCallDepth() > Constant.MAX_CALL_DEPTH) {
            log.error("[SEVM] Call depth exceeded: {}", Constant.MAX_CALL_DEPTH);
            return EVMResult.failed(new EVMException.CallStackOverflowException(), context);
        }

        // Initialize context for static call
        this.context = new EVMContext(new byte[0], gasLimit, from);
        context.setStaticCall(true);

        // Load contract bytecode
        byte[] contractCode;
        try {
            contractCode = context.getBlockchain().loadCode(to);
        } catch (EVMException.ContractNotFoundException e) {
            log.error("[SEVM] Contract not found: {}", to, e);
            return EVMResult.failed(e, context);
        }

        // Set up context
        context.setContractAddress(to);
        context.setCallData(callData);
        context.setByteCode(contractCode);
        context.setGasLimit(gasLimit);

        return executeInternal();
    }

    // ------------------------------------------------------------------------------------>

    /**
     * Internal execution method
     */
    private EVMResult executeInternal() {
        try {
            // Execute
            while (context.isRunning() && context.hasMoreCode()) {
                Opcode opcode = context.getCurrentOpcode();
                consumeGas(opcode);
                context.advanceCurrentPC();

                try {
                    dispatcher.dispatch(context, opcode);
                } catch (EVMException e) {
                    log.error("[SEVM] Error executing opcode: {}", opcode, e);
                    context.halt();
                    context.getCurrentFrame().setReverted(true, e.getMessage());
                    return EVMResult.failed(e, context);
                }
            }

            return EVMResult.OK(context);

        } catch (Exception e) {
            log.error("[SEVM] Execution failed", e);
            return EVMResult.failed(new EVMException.UnknownSystemException(e), context);
        }
    }

    private void consumeGas(Opcode opcode) {
        context.consumeGas(opcode.getGasCost());
        log.info("[SEVM] consumeGas: {} ({} gas), gasRemaining: {}",
                opcode, opcode.getGasCost(), context.getGasRemaining());
    }

    // Utility methods for debugging and inspection
    public long getGasRemaining() {
        return context != null ? context.getGasRemaining() : 0;
    }

    public long getTotalGasUsed() {
        return context != null ? context.getGasUsed() : 0;
    }

    public Stack<Long> getStack() {
        return context != null ? context.getCurrentStack() : null;
    }

    public List<LogEntry> getAllLogs() {
        return context != null ? context.getAllLogs() : List.of();
    }

    public void registerContract(Address contractAddress, byte[] contractBytecode) {
        if (context != null) {
            context.getBlockchain().registerContract(contractAddress, contractBytecode);
        }
    }

    // Debug methods
    public void printStack() {
        if (context != null) {
            context.getCurrentStack().printStack();
        }
    }

    public void printMemory() {
        if (context != null) {
            context.getCurrentMemory().printMemory();
        }
    }

    public void printStorage() {
        if (context != null) {
            context.getStorage().printStorage();
        }
    }
}
