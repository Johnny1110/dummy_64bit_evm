package com.frizo.lab.sevm.op;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OpcodeTest {

    @Test
    void isPush() {
        byte byteCode = Opcode.ADD.getCode();
        Opcode opcode = Opcode.fromByte(byteCode);
        boolean isPush = opcode.isPush();
        // assert that the opcode is not a PUSH opcode
        Assertions.assertFalse(isPush);

        Assertions.assertTrue(Opcode.PUSH1.isPush());
        Assertions.assertTrue(Opcode.PUSH4.isPush());

        Assertions.assertFalse(Opcode.UNKNOWN.isPush());
    }
}