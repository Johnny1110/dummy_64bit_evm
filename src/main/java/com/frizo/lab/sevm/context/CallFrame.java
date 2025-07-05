package com.frizo.lab.sevm.context;

public class CallFrame extends EVMContext {

    private int returnDataOffset;           // retuern data offset
    private int returnDataSize;             // return data size
    private boolean isContract;             // is contract call
    private byte[] contractAddress;         // contract address (if contract call)

    public CallFrame(byte[] bytecode, int initialGas) {
        super(bytecode, initialGas);
    }
}
