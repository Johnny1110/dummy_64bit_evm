package com.frizo.lab.sevm.context.call;

import com.frizo.lab.sevm.common.Address;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallData {
    private Address contractAddress;    // contract Address
    private Address caller;            // caller Address
    private Address origin;            // Txn Origin Address
    private long value;               // transfer value
    private byte[] inputData;        // input data
    private long inputOffset;         // input data offset in memory
    private long inputSize;           // input data size

    // Call Type
    private CallType callType;

    // Static Call (read-only)
    private boolean isStatic;
}
