package com.frizo.lab.nums;

public enum Opcode {
    STOP((byte) 0x00, 0),
    ADD((byte) 0x01, 3),
    MUL((byte) 0x02, 5),
    SUB((byte) 0x03, 5),
    DIV((byte) 0x04, 5),

    // PUSH1 ~ PUSH32
    PUSH1((byte) 0x60, 3), // 0x60 represents PUSH1, which pushes 1 byte onto the stack
    PUSH2((byte) 0x61, 3),
    PUSH3((byte) 0x62, 3),
    PUSH4((byte) 0x63, 3),
    PUSH5((byte) 0x64, 3),
    PUSH6((byte) 0x65, 3),
    PUSH7((byte) 0x66, 3),
    PUSH8((byte) 0x67, 3),
    PUSH9((byte) 0x68, 3),
    PUSH10((byte) 0x69, 3),
    PUSH11((byte) 0x6A, 3),
    PUSH12((byte) 0x6B, 3),
    PUSH13((byte) 0x6C, 3),
    PUSH14((byte) 0x6D, 3),
    PUSH15((byte) 0x6E, 3),
    PUSH16((byte) 0x6F, 3),
    PUSH17((byte) 0x70, 3),
    PUSH18((byte) 0x71, 3),
    PUSH19((byte) 0x72, 3),
    PUSH20((byte) 0x73, 3),
    PUSH21((byte) 0x74, 3),
    PUSH22((byte) 0x75, 3),
    PUSH23((byte) 0x76, 3),
    PUSH24((byte) 0x77, 3),
    PUSH25((byte) 0x78, 3),
    PUSH26((byte) 0x79, 3),
    PUSH27((byte) 0x7A, 3),
    PUSH28((byte) 0x7B, 3),
    PUSH29((byte) 0x7C, 3),
    PUSH30((byte) 0x7D, 3),
    PUSH31((byte) 0x7E, 3),
    PUSH32((byte) 0x7F, 3),


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

    public boolean isPush() {
        return this.code >= PUSH1.code && this.code <= PUSH32.code;
    }
}
