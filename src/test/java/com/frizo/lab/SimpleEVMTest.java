package com.frizo.lab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleEVMTest {


    @Test
    void testSimpleEVM() {
        // Example bytecode: PUSH1 0x05, PUSH1 0x03, ADD, STOP
        byte[] bytecode = new byte[] {0x60, 0x05, 0x60, 0x03, 0x01, 0x00};

        SimpleEVM evm = new SimpleEVM(bytecode, 10000);
        evm.run();

        // The result of ADD (5 + 3) should be on the stack
        assertEquals(8, evm.peek());

        // Print the stack for verification
        evm.printStack();
    }

    @Test
    void testMemoryOperations() {
        byte[] bytecode = {
                0x60, 0x2A,       // PUSH1 42
                0x60, 0x00,       // PUSH1 0 (offset)
                0x52,             // MSTORE

                0x60, 0x00,       // PUSH1 0
                0x51,             // MLOAD
                0x00              // STOP
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 10000);
        evm.run();
        evm.printStack();  // Expected: [42]
        evm.printMemory(); // Expected: memory[0] contains 32 bytes with last 4 = 0x0000002A
    }

    @Test
    void testJumpOperations() {
        byte[] bytecode = {
                0x60, 0x01,       // PUSH1 1      (condition)
                0x60, 0x08,       // PUSH1 8      (jump dest)
                0x57,             // JUMPI        if 1 != 0 → jump to pc=8
                0x60, 0x2A,       // PUSH1 42     (jump target)
                0x00,             // STOP         (this stop will be jumped over)
                0x5B,             // JUMPDEST     (pc = 8)
                0x60, 0x2B,       // PUSH1 43
                0x00              // STOP
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 10000);
        evm.run();
        evm.printStack(); // Expected: [43]
    }

    @Test
    void testSSTOREAndSLOAD() {
        byte[] bytecode = {
                0x60, 0x2A,       // PUSH1 42         ← value
                0x60, 0x01,       // PUSH1 1          ← key
                0x55,             // SSTORE (storage[1] = 42)

                0x60, 0x01,       // PUSH1 1
                0x54,             // SLOAD  (→ stack ← 42)
                0x00              // STOP
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 200);
        evm.run();
        evm.printStack();   // Expected: [42]
        evm.printStorage(); // Expected: {1=42}

        System.out.println("Gas left: " + evm.getGasRemaining());
    }

    @Test
    void testOutofGas() {
        byte[] bytecode = {
                0x60, 0x01,       // PUSH1 1
                0x60, 0x02,       // PUSH1 2
                0x01,             // ADD
                0x00              // STOP
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 1); // Set gas limit too low
        Exception exception = assertThrows(RuntimeException.class, evm::run);
        assertTrue(exception.getMessage().contains("Out of gas"));
    }

}