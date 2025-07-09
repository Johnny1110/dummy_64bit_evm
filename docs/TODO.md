TODO: Refactor SEVM

```java
package com.frizo.lab.sevm.vm;

import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.exec.InstructionDispatcher;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.List;

@Slf4j
public class SimpleEVM {

    @Getter
    private EVMContext context;
    private final InstructionDispatcher dispatcher;

    public SimpleEVM() {
        this.dispatcher = new InstructionDispatcher();
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
                                      BigInteger value, long gasLimit) {
        // Initialize context for this transaction
        this.context = new EVMContext(data, gasLimit, from);
        
        if (to == null) {
            // Contract creation
            return executeContractCreation(from, data, value, gasLimit);
        } else {
            // Contract call
            return executeContractCall(from, to, data, value, gasLimit);
        }
    }

    /**
     * Execute contract creation
     */
    private EVMResult executeContractCreation(Address from, byte[] initCode, 
                                            BigInteger value, long gasLimit) {
        log.info("[SimpleEVM] Executing contract creation from: {}", from);
        
        // Set up context for contract creation
        context.getCurrentFrame().setCreationMode(true);
        context.getCurrentFrame().setValue(value);
        
        return executeInternal(initCode);
    }

    /**
     * Execute contract call
     */
    private EVMResult executeContractCall(Address from, Address to, byte[] calldata, 
                                        BigInteger value, long gasLimit) {
        log.info("[SimpleEVM] Executing contract call from: {} to: {}", from, to);
        
        // Load contract bytecode from blockchain state
        byte[] contractCode = context.getBlockchain().getContractCode(to);
        if (contractCode == null || contractCode.length == 0) {
            return new EVMResult(false, "Contract not found", null, context.getGasUsed(), getAllLogs());
        }
        
        // Set up context for contract call
        context.getCurrentFrame().setCalldata(calldata);
        context.getCurrentFrame().setValue(value);
        context.getCurrentFrame().setCodeAddress(to);
        
        return executeInternal(contractCode);
    }

    /**
     * Internal execution method
     */
    private EVMResult executeInternal(byte[] bytecode) {
        try {
            // Set the bytecode to execute
            context.setCurrentCode(bytecode);
            
            // Pre-process bytecode
            preHandle();
            
            // Execute
            while (context.isRunning() && context.hasMoreCode()) {
                Opcode opcode = context.getCurrentOpcode();
                consumeGas(opcode);
                context.advanceCurrentPC();

                try {
                    dispatcher.dispatch(context, opcode);
                } catch (Exception e) {
                    log.error("[SimpleEVM] Error executing opcode: {}", opcode, e);
                    context.halt();
                    context.getCurrentFrame().setReverted(true, e.getMessage());
                    return new EVMResult(false, e.getMessage(), null, context.getGasUsed(), getAllLogs());
                }
            }
            
            // Handle return data
            byte[] returnData = context.getCurrentFrame().getReturnData();
            boolean success = !context.getCurrentFrame().isReverted();
            
            return new EVMResult(success, null, returnData, context.getGasUsed(), getAllLogs());
            
        } catch (Exception e) {
            log.error("[SimpleEVM] Execution failed", e);
            return new EVMResult(false, e.getMessage(), null, context.getGasUsed(), getAllLogs());
        }
    }

    /**
     * Execute a static call (read-only)
     */
    public EVMResult staticCall(Address from, Address to, byte[] calldata, long gasLimit) {
        log.info("[SimpleEVM] Executing static call from: {} to: {}", from, to);
        
        // Initialize context for static call
        this.context = new EVMContext(new byte[0], gasLimit, from);
        context.getCurrentFrame().setStaticCall(true);
        
        // Load contract bytecode
        byte[] contractCode = context.getBlockchain().getContractCode(to);
        if (contractCode == null || contractCode.length == 0) {
            return new EVMResult(false, "Contract not found", null, 0, getAllLogs());
        }
        
        // Set up context
        context.getCurrentFrame().setCalldata(calldata);
        context.getCurrentFrame().setCodeAddress(to);
        
        return executeInternal(contractCode);
    }

    /**
     * Get the current state of the EVM
     */
    public EVMState getState() {
        if (context == null) {
            return null;
        }
        return new EVMState(
            context.getCurrentStack().copy(),
            context.getCurrentMemory().copy(),
            context.getStorage().copy(),
            context.getGasRemaining(),
            context.getCurrentFrame().getProgramCounter()
        );
    }

    /**
     * scan the bytecode to find valid jump destinations
     */
    private void preHandle() {
        log.info("[SimpleEVM] Pre-handling bytecode to find valid jump destinations...");
        for (int i = 0; i < context.getCurrentCode().length; i++) {
            if (context.getCurrentCode()[i] == Opcode.JUMPDEST.getCode()) {
                context.getValidJumpDestIdx().add(i);
            }
        }
    }

    private void consumeGas(Opcode opcode) {
        context.consumeGas(opcode.getGasCost());
        log.info("[SimpleEVM] consumeGas: {} ({} gas), gasRemaining: {}", 
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

// Execution result class
class EVMResult {
    @Getter
    private final boolean success;
    @Getter
    private final String error;
    @Getter
    private final byte[] returnData;
    @Getter
    private final long gasUsed;
    @Getter
    private final List<LogEntry> logs;

    public EVMResult(boolean success, String error, byte[] returnData, 
                    long gasUsed, List<LogEntry> logs) {
        this.success = success;
        this.error = error;
        this.returnData = returnData;
        this.gasUsed = gasUsed;
        this.logs = logs;
    }
}

// EVM state snapshot class
class EVMState {
    @Getter
    private final Stack<Long> stack;
    @Getter
    private final Object memory; // Your memory implementation
    @Getter
    private final Object storage; // Your storage implementation
    @Getter
    private final long gasRemaining;
    @Getter
    private final int programCounter;

    public EVMState(Stack<Long> stack, Object memory, Object storage, 
                   long gasRemaining, int programCounter) {
        this.stack = stack;
        this.memory = memory;
        this.storage = storage;
        this.gasRemaining = gasRemaining;
        this.programCounter = programCounter;
    }
}
```

Usage:

```java
SimpleEVM evm = new SimpleEVM();
EVMResult result = evm.executeTransaction(
    fromAddress, 
    toAddress, 
    calldata, 
    value, 
    gasLimit
);
```