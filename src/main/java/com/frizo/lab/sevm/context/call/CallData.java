package com.frizo.lab.sevm.context.call;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallData {
    private String contractAddress;    // contract Address
    private String caller;            // caller Address
    private String origin;            // Txn Origin Address
    private int value;               // transfer value
    private byte[] inputData;        // input data
    private int inputOffset;         // input data offset in memory
    private int inputSize;           // input data size

    // Call Type
    private CallType callType;

    // Static Call (read-only)
    private boolean isStatic;
}
