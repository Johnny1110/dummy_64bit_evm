package com.frizo.lab.nums;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OpcodeTest {

    @Test
    void isPush() {
        byte byteCode = 0x11;
        Opcode opcode = Opcode.fromByte(byteCode);
        boolean isPush = opcode.isPush();
        // assert that the opcode is not a PUSH opcode
        Assertions.assertFalse(isPush);
    }
}