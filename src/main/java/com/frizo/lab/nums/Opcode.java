package com.frizo.lab.nums;

public enum Opcode {
    STOP((byte) 0x00, 0),
    ADD((byte) 0x01, 3),
    MUL((byte) 0x02, 5),
    SUB((byte) 0x03, 5),
    DIV((byte) 0x04, 5),
    PUSH1((byte) 0x60, 3), // 0x60 represents PUSH1, which pushes 1 byte onto the stack

    MSTORE((byte) 0x52, 12),
    MLOAD((byte) 0x51, 3),

    SSTORE((byte) 0x55, 20), // Store a value from the stack into memory
    SLOAD((byte) 0x54, 50), // Load a value from memory onto the stack

    JUMP((byte) 0x56, 8), // Stack: [dest] → JUMP → pc = dest
    JUMPI((byte) 0x57, 10), // Stack: [dest, condition] → JUMPI → if condition != 0 then pc = dest
    JUMPDEST((byte) 0x5B, 1) // mark a valid jump destination, no effect on stack or pc

    ;

    Opcode(byte code, int gasCost) {
        this.code = code;
        this.gasCost = gasCost;
    }

    private final byte code;
    private final int gasCost;

    public static Opcode fromByte(byte b) {
        for (Opcode op : values()) {
            if (op.code == b) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown opcode: " + String.format("0x%02X", b));
    }

    public byte getCode() {
        return code;
    }

    public int getGasCost() {
        return gasCost;
    }
}
