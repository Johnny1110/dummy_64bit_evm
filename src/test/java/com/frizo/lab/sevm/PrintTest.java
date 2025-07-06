package com.frizo.lab.sevm;

import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.vm.SimpleEVM;
import org.junit.jupiter.api.Test;

public class PrintTest {

    private static final String TEST_ORIGIN = "0x12345678";

    @Test
    void testPrint() {

        byte[] bytecode = {
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x64,
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x63,
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x62,
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x61,
                Opcode.PUSH1.getCode(), 0x04,       // size 4
                Opcode.PRINT.getCode(),
                Opcode.STOP.getCode()
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 10000000, TEST_ORIGIN);
        evm.run();
    }
}
