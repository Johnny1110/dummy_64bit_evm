package com.frizo.lab.sevm.op;

import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.exec.impl.*;
import lombok.Getter;

public enum Opcode {

    STOP((byte) 0x00, 0, StopExecutor.class),

    // Arithmetic operations (0x01 ~ 0x04)
    ADD((byte) 0x01, 3, ArithmeticExecutor.class),
    MUL((byte) 0x02, 5, ArithmeticExecutor.class),
    SUB((byte) 0x03, 5, ArithmeticExecutor.class),
    DIV((byte) 0x04, 5, ArithmeticExecutor.class),

    LT((byte) 0x10, 3, NumLogicInstruction.class), // Less than
    GT((byte) 0x11, 3, NumLogicInstruction.class), // Greater
    SLT((byte) 0x12, 3, NumLogicInstruction.class), // Signed less than
    SGT((byte) 0x13, 3, NumLogicInstruction.class), //
    EQ((byte) 0x14, 3, NumLogicInstruction.class), // Equal
    ISZERO((byte) 0x15, 3, NumLogicInstruction.class), // NOT
    AND((byte) 0x16, 3, NumLogicInstruction.class), // Bitwise AND
    OR((byte) 0x17, 3, NumLogicInstruction.class), // Bit
    XOR((byte) 0x18, 3, NumLogicInstruction.class), // Bitwise XOR

    POP((byte) 0x50, 2, PopExecutor.class), // Pop the top value from the stack
    // 0x51 0x52 memory
    MLOAD((byte) 0x51, 3, MemoryExecutor.class),
    MSTORE((byte) 0x52, 12, MemoryExecutor.class),

    // Storage operations (0x54, 0x55)
    SLOAD((byte) 0x54, 100, StorageExecutor.class), // Load a value from memory onto the stack
    SSTORE((byte) 0x55, 20000, StorageExecutor.class), // Store a value from the stack into memory

    // JUMP
    JUMP((byte) 0x56, 8, JumpExecutor.class), // Stack: [dest] → JUMP → pc = dest
    JUMPI((byte) 0x57, 10, JumpExecutor.class), // Stack: [dest, condition] → JUMPI → if condition != 0 then pc = dest
    JUMPDEST((byte) 0x5B, 1, JumpExecutor.class), // mark a valid jump destination, no effect on stack or pc

    // PUSH1 ~ PUSH4 (32bit stack push opcodes)
    // 0x60 represents PUSH1, which pushes 1 byte onto the stack
    PUSH1((byte) 0x60, 3, PushExecutor.class),
    PUSH2((byte) 0x61, 3, PushExecutor.class),
    PUSH3((byte) 0x62, 3, PushExecutor.class),
    PUSH4((byte) 0x63, 3, PushExecutor.class),
    PUSH5((byte) 0x64, 3, PushExecutor.class),
    PUSH6((byte) 0x65, 3, PushExecutor.class),
    PUSH7((byte) 0x66, 3, PushExecutor.class),
    PUSH8((byte) 0x67, 3, PushExecutor.class),

    // DUP1~DUP16 (0x80 ~ 0x8f)
    DUP1((byte) 0x80, 3, DupExecutor.class),
    DUP2((byte) 0x81, 3, DupExecutor.class),
    DUP3((byte) 0x82, 3, DupExecutor.class),
    DUP4((byte) 0x83, 3, DupExecutor.class),
    DUP5((byte) 0x84, 3, DupExecutor.class),
    DUP6((byte) 0x85, 3, DupExecutor.class),
    DUP7((byte) 0x86, 3, DupExecutor.class),
    DUP8((byte) 0x87, 3, DupExecutor.class),
    DUP9((byte) 0x88, 3, DupExecutor.class),
    DUP10((byte) 0x89, 3, DupExecutor.class),
    DUP11((byte) 0x8A, 3, DupExecutor.class),
    DUP12((byte) 0x8B, 3, DupExecutor.class),
    DUP13((byte) 0x8C, 3, DupExecutor.class),
    DUP14((byte) 0x8D, 3, DupExecutor.class),
    DUP15((byte) 0x8E, 3, DupExecutor.class),
    DUP16((byte) 0x8F, 3, DupExecutor.class),

    // SWAPx (0x90 ~ 0x9f)
    SWAP1((byte) 0x90, 3, SwapExecutor.class),
    SWAP2((byte) 0x91, 3, SwapExecutor.class),
    SWAP3((byte) 0x92, 3, SwapExecutor.class),
    SWAP4((byte) 0x93, 3, SwapExecutor.class),
    SWAP5((byte) 0x94, 3, SwapExecutor.class),
    SWAP6((byte) 0x95, 3, SwapExecutor.class),
    SWAP7((byte) 0x96, 3, SwapExecutor.class),
    SWAP8((byte) 0x97, 3, SwapExecutor.class),
    SWAP9((byte) 0x98, 3, SwapExecutor.class),
    SWAP10((byte) 0x99, 3, SwapExecutor.class),
    SWAP11((byte) 0x9A, 3, SwapExecutor.class),
    SWAP12((byte) 0x9B, 3, SwapExecutor.class),
    SWAP13((byte) 0x9C, 3, SwapExecutor.class),
    SWAP14((byte) 0x9D, 3, SwapExecutor.class),
    SWAP15((byte) 0x9E, 3, SwapExecutor.class),
    SWAP16((byte) 0x9F, 3, SwapExecutor.class),

    // CALL
    CALL((byte) 0xF1, 40, CallExecutor.class),           // 外部合約調用 external contract call
    CALLCODE((byte) 0xF2, 40, CallExecutor.class),       // 調用代碼但在當前上下文執行
    DELEGATECALL((byte) 0xF4, 40, CallExecutor.class),   // 委託調用
    STATICCALL((byte) 0xFA, 40, CallExecutor.class),     // 靜態調用

    // Internal func call (Custom opcode)
    ICALL((byte) 0xFC, 10, CallExecutor.class),          // 內部調用
    RETURN((byte) 0xF3, 0, ReturnRevertExecutor.class),        // Return from a function call
    REVERT((byte) 0xFD, 0, ReturnRevertExecutor.class),        // Revert a function call, used for error handling

    // LOGn (0xA0 ~ 0xA4)
    LOG0((byte) 0xA0, 375, LogExecutor.class),
    LOG1((byte) 0xA1, 750, LogExecutor.class),
    LOG2((byte) 0xA2, 1125, LogExecutor.class),
    LOG3((byte) 0xA3, 1500, LogExecutor.class),
    LOG4((byte) 0xA4, 1875, LogExecutor.class),

    // System operations
    PRINT((byte) 0xF0, 0, PrintExecutor.class), // Custom opcode for printing stack values
    UNKNOWN((byte) 0xFF, 0, null),
    ;

    @Getter
    private final byte code;
    @Getter
    private final int gasCost;
    @Getter
    private final Class<? extends InstructionExecutor> executorClass;

    Opcode(byte code, int gasCost, Class<? extends InstructionExecutor> executorClass) {
        this.code = code;
        this.gasCost = gasCost;
        this.executorClass = executorClass;
    }

    public static Opcode fromByte(byte b) {
        for (Opcode op : values()) {
            if (op.code == b) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown opcode: " + String.format("0x%02X", b));
    }

    public static boolean isNumLogic(Opcode opcode) {
        return opcode == LT || opcode == GT || opcode == SLT || opcode == SGT ||
                opcode == EQ || opcode == ISZERO ||
                opcode == AND || opcode == OR || opcode == XOR;
    }

    public boolean isPush() {
        // 32 bit stack push opcodes are from 0x60 (PUSH1) to 0x63 (PUSH4)
        return this.code >= PUSH1.code && this.code <= PUSH8.code;
    }

    public boolean isDup() {
        // DUP1~DUP16 are from 0x80 to 0x8F
        return this.code >= DUP1.code && this.code <= DUP16.code;
    }

    public boolean isSwap() {
        // SWAP1~SWAP16 are from 0x90 to 0x9F
        return this.code >= SWAP1.code && this.code <= SWAP16.code;
    }

    public boolean isCall() {
        return this == CALL || this == CALLCODE || this == DELEGATECALL ||
                this == STATICCALL || this == ICALL;
    }

    public boolean isReturn() {
        return this == RETURN || this == REVERT;
    }

    public boolean isLog() {
        // LOG0~LOG4 are from 0xA0 to 0xA4
        return this.code >= LOG0.code && this.code <= LOG4.code;
    }
}
