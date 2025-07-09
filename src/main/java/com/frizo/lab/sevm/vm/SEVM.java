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

    public SEVM() {
        this.dispatcher = new InstructionDispatcher();
    }

    // ------------------------------------------------------------------------------------>

    public EVMResult create(Address caller, Address contractAddress, byte[] code, long value, long gasLimit) {
        log.info("[SEVM] Creating contract from: {}", caller);
        this.context = new EVMContext(new byte[]{}, value, gasLimit, caller);
        // Initialize context for contract creation
        return executeContractCreation(caller, contractAddress, code, value);
    }

    /**
     * Execute a transaction (similar to eth_call or contract creation)
     * @param from sender address
     * @param to recipient address (null for contract creation)
     * @param data transaction data (bytecode for creation, calldata for calls)
     * @param value ether value to transfer
     * @param gasLimit gas limit for execution
     * @return execution result
     */
    public EVMResult executeTransaction(Address from, Address to, byte[] data,
                                        long value, long gasLimit) {
        if (from == null || data == null || gasLimit <= 0) {
            throw new IllegalArgumentException("Invalid transaction parameters");
        }
        this.context = new EVMContext(data, value, gasLimit, from);
        return executeContractCall(from, to, data, value);
    }

    /**
     * Execute a static call (read-only)
     */
    public EVMResult staticCall(Address from, Address to, byte[] callData, long gasLimit) {
        log.info("[SEVM] Executing static call from: {} to: {}", from, to);

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

        return executeInternal(contractCode);
    }

    // ------------------------------------------------------------------------------------>

    /**
     * Execute contract creation
     */
    private EVMResult executeContractCreation(Address from, Address creationAddress, byte[] initCode, long value) {
        log.info("[SEVM] Executing contract creation from: {}", from);
        if (context.getDepth() > Constant.MAX_STACK_DEPTH) {
            return EVMResult.failed(new EVMException.StackOverflowException(), context);
        }

        // check balance
        if (!context.getBlockchain().canTransfer(from, value)) {
            throw new EVMException.ErrInsufficientBalance(from);
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
            context.getBlockchain().setNonce(from, context.getBlockchain().getNonce(from) + 1);

            // 6. create contract address
            context.getBlockchain().createContract(creationAddress);

            // 7. setup nonce for address 1（EIP-161）
            context.getBlockchain().setNonce(creationAddress, 1);

            // 8. transfer value from caller to contract address
            context.getBlockchain().transfer(from, creationAddress, value);

            // 9. set up context for contract creation
            context.creationMode();
            context.setValue(value);
            context.setContractAddress(creationAddress);
            context.setByteCode(initCode);

            // 10. init contract (constructor)
            EVMResult result = executeInternal(initCode);

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

    /**
     * Execute contract call
     */
    private EVMResult executeContractCall(Address from, Address to, byte[] callData, long value) {
        log.info("[SEVM] Executing contract call from: {} to: {}", from, to);

        // Load contract bytecode from blockchain state
        byte[] contractCode;
        try{
            contractCode = context.getBlockchain().loadCode(to);
            if (contractCode == null || contractCode.length == 0) {
                throw new EVMException.ContractNotFoundException("Contract not found: " + to);
            }
        } catch (EVMException.ContractNotFoundException e) {
            log.error("[SEVM] Contract not found: {}", to, e);
            return EVMResult.failed(e, context);
        }

        // Set up context for contract call
        context.setByteCode(contractCode);
        context.setCallData(callData);
        context.setContractAddress(to);
        context.setValue(value);

        return executeInternal(contractCode);
    }

    /**
     * Internal execution method
     */
    private EVMResult executeInternal(byte[] bytecode) {
        try {
            // Set the bytecode to execute
            context.setByteCode(bytecode);
            // Pre-process bytecode
            context.preExecHandle();

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
