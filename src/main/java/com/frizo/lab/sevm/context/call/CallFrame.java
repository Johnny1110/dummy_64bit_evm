package com.frizo.lab.sevm.context.call;

import com.frizo.lab.sevm.context.EVMContext;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CallFrame extends EVMContext {

    // Call Data
    private final String contractAddress;    // contract Address
    private final String caller;            // caller Address
    private final String origin;            // Txn Origin Address
    private final int value;               // transfer value
    private final byte[] inputData;        // input data
    private final int inputOffset;         // input data offset in memory
    private final int inputSize;           // input data size

    // Call Result
    @Setter
    private boolean success;

    // Call Type
    private final CallType callType;

    // Static Call (read-only)
    private final boolean isStatic;

    public CallFrame(byte[] bytecode, int initialGas, CallData callData) {
        super(bytecode, initialGas);

        this.contractAddress = callData.getContractAddress();
        this.caller = callData.getCaller();
        this.origin = callData.getOrigin();
        this.value = callData.getValue();
        this.inputData = callData.getInputData();
        this.inputOffset = callData.getInputOffset();
        this.inputSize = callData.getInputSize();
        this.callType = callData.getCallType();
        this.isStatic = callData.isStatic();
    }


}
