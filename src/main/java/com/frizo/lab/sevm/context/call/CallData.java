package com.frizo.lab.sevm.context.call;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallData {
    private String contractAddress;    // contract Address
    private String caller;            // caller Address
    private String origin;            // Txn Origin Address
    private long value;               // transfer value
    private byte[] inputData;        // input data
    private long inputOffset;         // input data offset in memory
    private long inputSize;           // input data size

    // Call Type
    private CallType callType;

    // Static Call (read-only)
    private boolean isStatic;
}
