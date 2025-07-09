package com.frizo.lab.sevm.sevm_v1;

import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.vm.SimpleEVM;
import org.junit.jupiter.api.Test;

import static com.frizo.lab.sevm.TestConstant.TEST_ORIGIN;
import static org.junit.jupiter.api.Assertions.*;

class SimpleEVMTest {


    @Test
    void testSimpleEVM() {
        // Example bytecode: PUSH1 0x05, PUSH1 0x03, ADD, STOP
        byte[] bytecode = new byte[]{0x60, 0x05, 0x60, 0x03, 0x01, 0x00};

        SimpleEVM evm = new SimpleEVM(bytecode, 10000, TEST_ORIGIN);
        evm.run();

        // The result of ADD (5 + 3) should be on the stack
        assertEquals(8, evm.peek());

        // Print the stack for verification
        evm.printStack();
        System.out.println("total gas used: " + evm.totalGasUsed());
    }

    @Test
    void testPush2() {
        // Example bytecode: PUSH2 0x1234 (4660), STOP
        byte[] bytecode = new byte[]{0x61, 0x12, 0x34, 0x00};

        SimpleEVM evm = new SimpleEVM(bytecode, 10000, TEST_ORIGIN);
        evm.run();

        // The result of PUSH2 should be on the stack
        assertEquals(0x1234, evm.peek());

        // Print the stack for verification
        evm.printStack();
    }

    @Test
    void testPush3() {
        // Example bytecode: PUSH3 0x123456 (1193046), STOP
        byte[] bytecode = new byte[]{0x62, 0x12, 0x34, 0x56, 0x00};

        SimpleEVM evm = new SimpleEVM(bytecode, 10000, TEST_ORIGIN);
        evm.run();

        // The result of PUSH2 should be on the stack
        assertEquals(0x123456, evm.peek());

        // Print the stack for verification
        evm.printStack();
    }

    @Test
    void testPush4() {
        // Example bytecode: PUSH4 0x12345678 (305419896), STOP
        byte[] bytecode = new byte[]{0x63, 0x12, 0x34, 0x56, 0x78, 0x00};

        SimpleEVM evm = new SimpleEVM(bytecode, 10000, TEST_ORIGIN);
        evm.run();

        // The result of PUSH2 should be on the stack
        assertEquals(0x12345678, evm.peek());

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

        SimpleEVM evm = new SimpleEVM(bytecode, 10000, TEST_ORIGIN);
        evm.run();
        evm.getStack();
        assertEquals(42, evm.peek()); // The value 42 should be loaded onto the stack
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

        SimpleEVM evm = new SimpleEVM(bytecode, 10000, TEST_ORIGIN);
        evm.run();
        evm.printStack(); // Expected: [43]
    }

    @Test
    void testSSTOREAndSLOAD() {
        byte[] bytecode = {
                0x60, 0x2A,       // PUSH1 42         ← value
                0x60, 0x01,       // PUSH1 1          ← key
                0x55,             // SSTORE (storage[1~8] = 0x0000002A)

                0x60, 0x01,       // PUSH1 1
                0x54,             // SLOAD  (→ stack ← 42)
                0x00              // STOP
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 20000000, TEST_ORIGIN);
        evm.run();
        evm.printStack();   // Expected: [42]
        evm.printStorage(); // Expected: {1=42}
        evm.printStack();
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

        SimpleEVM evm = new SimpleEVM(bytecode, 1, TEST_ORIGIN); // Set gas limit too low
        Exception exception = assertThrows(RuntimeException.class, evm::run);
        assertTrue(exception.getMessage().contains("Out of gas"));
    }

    @Test
    void testDuplicateOperations() {
        byte[] bytecode = {
                Opcode.PUSH1.getCode(), 0x05,       // PUSH1 5
                Opcode.PUSH1.getCode(), 0x0A,       // PUSH1 10
                Opcode.PUSH1.getCode(), 0x0F,       // PUSH1 15
                Opcode.DUP2.getCode(),             // DUP1
                Opcode.STOP.getCode()              // STOP
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 10000, TEST_ORIGIN);
        evm.run();
        evm.printStack(); // Expected: [1, 2, 3, 3]
    }

    @Test
    void testDUP2() {
        byte[] bytecode = new byte[]{
                0x60, 0x0a,             // PUSH1 10
                0x60, 0x14,             // PUSH1 20
                (byte) 0x81,            // DUP2 (10)
                0x00                    // STOP
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        evm.printStack();

        // 預期堆疊為 [10, 20, 10]，但注意 LIFO 順序，top 是最後 push 的值
        assertEquals(3, evm.getStack().size());
        assertEquals(10, evm.getStack().safePop()); // DUP2
        assertEquals(20, evm.getStack().safePop()); // PUSH1 20
        assertEquals(10, evm.getStack().safePop()); // PUSH1 10
    }

    @Test
    public void testSWAP1() {
        byte[] bytecode = new byte[]{
                0x60, 0x01,       // PUSH1 1
                0x60, 0x02,       // PUSH1 2
                (byte) 0x90,      // SWAP1: swap top with 2nd
                0x00              // STOP
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        assertEquals(2, evm.getStack().size());
        assertEquals(1, evm.getStack().safePop()); // top after SWAP: 1
        assertEquals(2, evm.getStack().safePop()); // second after SWAP: 2
    }

    @Test
    public void testSWAP2() {
        byte[] bytecode = new byte[]{
                0x60, 0x01,       // PUSH1 1
                0x60, 0x02,       // PUSH1 2
                0x60, 0x03,       // PUSH1 3
                (byte) 0x91,      // SWAP2: swap top with 3rd
                0x00
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        assertEquals(3, evm.getStack().size());
        assertEquals(1, evm.getStack().safePop()); // top: 1
        assertEquals(2, evm.getStack().safePop()); // middle: 2
        assertEquals(3, evm.getStack().safePop()); // bottom
    }

    @Test
    public void testSWAP3() {
        byte[] bytecode = new byte[]{
                0x60, 0x10,       // PUSH1 16
                0x60, 0x20,       // PUSH1 32
                0x60, 0x30,       // PUSH1 48
                0x60, 0x40,       // PUSH1 64
                (byte) 0x92,      // SWAP3: swap top (64) with 4th (16)
                0x00
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        evm.printStack();

        assertEquals(4, evm.getStack().size());
        assertEquals(16, evm.getStack().safePop());  // 64 <-> 16
        assertEquals(48, evm.getStack().safePop());
        assertEquals(32, evm.getStack().safePop());
        assertEquals(64, evm.getStack().safePop());
    }

}