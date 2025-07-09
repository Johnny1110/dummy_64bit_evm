package com.frizo.lab.sevm.vm;

import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.log.LogEntry;
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
        this.context = new EVMContext(data, value, gasLimit, from);
        if (to == null) {
            // Contract creation
            return executeContractCreation(from, data);
        } else {
            // Contract call
            return executeContractCall(from, to, data);
        }
    }

    /**
     * Execute contract creation
     */
    private EVMResult executeContractCreation(Address from, byte[] initCode) {
        log.info("[SimpleEVM] Executing contract creation from: {}", from);
        // Set up context for contract creation
        context.getCurrentFrame().enableCreationMode();
        return executeInternal(initCode);
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
}
