package com.frizo.lab.sevm.common;

public class Constant {

    // Maximum bytes for a single memory address
    public static final int MAX_BYTES = 4;
    public static final int MAX_STACK_DEPTH = 1024;
    public static final int MAX_CODE_SIZE = 1024 * 1024 * 5; // 5 MB
    public static final int CREATE_DATA_GAS = 32000;
    public static final int MAX_CALL_DEPTH = 1024;
}
