package com.frizo.lab.sevm.context.call;

public enum CallType {
    CALL,           // External function call
    CALLCODE,       // Call code but execute in current context
    DELEGATECALL,   // Delegate call to another contract
    STATICCALL,     // Static call (read-only)
    INTERNAL        // Internal call (within the same contract)
}
