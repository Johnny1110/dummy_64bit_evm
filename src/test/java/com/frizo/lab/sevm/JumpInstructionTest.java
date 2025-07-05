package com.frizo.lab.sevm;

import com.frizo.lab.sevm.nums.Opcode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JumpInstructionTest {

    private SimpleEVM evm;
    private static final int INITIAL_GAS = 1000000;

    @BeforeEach
    void setUp() {
        // 每個測試前重置 EVM
    }

    @Test
    @DisplayName("測試基本 JUMP 指令 - 跳轉到有效的 JUMPDEST")
    void testBasicJump() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x05    // 推送跳轉目標地址 5
         * 2: JUMP          // 跳轉指令
         * 3: PUSH1 0x99    // 這行不應該被執行
         * 5: JUMPDEST      // 跳轉目標
         * 6: PUSH1 0x42    // 跳轉後要執行的指令
         * 8: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x05,       // PUSH1 0x05
                0x56,             // JUMP
                0x60, (byte) 0x99,       // PUSH1 0x99 (shouldn't execute)
                0x5B,             // JUMPDEST
                0x60, 0x42,       // PUSH1 0x42
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證跳轉成功，堆疊頂部應該是 0x42，而不是 0x99
        assertEquals(0x42, evm.peek());
        assertTrue(evm.getGasRemaining() < INITIAL_GAS);
    }

    @Test
    @DisplayName("測試 JUMPI 指令 - 條件為真時跳轉")
    void testJumpiWithTrueCondition() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x01    // 推送條件值 1 (true)
         * 2: PUSH1 0x08    // 推送跳轉目標地址 8
         * 4: JUMPI         // 條件跳轉指令
         * 5: PUSH1 0x99    // 這行不應該被執行
         * 7: STOP          // 這行不應該被執行
         * 8: JUMPDEST      // 跳轉目標
         * 9: PUSH1 0x42    // 跳轉後要執行的指令
         * 11: STOP         // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x01,       // PUSH1 0x01
                0x60, 0x08,       // PUSH1 0x08
                0x57,             // JUMPI
                0x60, (byte) 0x99,       // PUSH1 0x99 (shouldn't execute)
                0x00,             // STOP (shouldn't execute)
                0x5B,             // JUMPDEST
                0x60, 0x42,       // PUSH1 0x42
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證條件跳轉成功
        assertEquals(0x42, evm.peek());
    }

    @Test
    @DisplayName("測試 JUMPI 指令 - 條件為假時不跳轉")
    void testJumpiWithFalseCondition() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x00    // 推送條件值 0 (false)
         * 2: PUSH1 0x08    // 推送跳轉目標地址 8
         * 4: JUMPI         // 條件跳轉指令
         * 5: PUSH1 0x99    // 這行應該被執行
         * 7: STOP          // 停止執行
         * 8: JUMPDEST      // 跳轉目標
         * 9: PUSH1 0x42    // 這行不應該被執行
         * 11: STOP         // 這行不應該被執行
         */
        byte[] bytecode = {
                0x60, 0x00,       // PUSH1 0x00
                0x60, 0x08,       // PUSH1 0x08
                0x57,             // JUMPI
                0x60, (byte) 0x99,       // PUSH1 0x99 (should execute)
                0x00,             // STOP
                0x5B,             // JUMPDEST
                0x60, 0x42,       // PUSH1 0x42 (shouldn't execute)
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證沒有跳轉，執行了 PUSH1 0x99
        assertEquals(0x99, evm.peek());
    }

    @Test
    @DisplayName("測試跳轉到無效目標 - 應該拋出異常")
    void testJumpToInvalidDestination() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x04    // 推送無效的跳轉目標地址 4
         * 2: JUMP          // 跳轉指令
         * 3: STOP          // 停止執行
         * 4: PUSH1 0x42    // 地址 4 沒有 JUMPDEST，是無效目標
         */
        byte[] bytecode = {
                0x60, 0x04,       // PUSH1 0x04
                0x56,             // JUMP
                0x00,             // STOP
                0x60, 0x42        // PUSH1 0x42 (invalid jump destination)
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);

        // 驗證跳轉到無效目標會拋出異常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            evm.run();
        });

        assertTrue(exception.getMessage().contains("Invalid jump destination"));
    }

    @Test
    @DisplayName("測試跳轉到超出字節碼範圍的地址")
    void testJumpToOutOfBounds() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0xFF    // 推送超出範圍的地址 255
         * 2: JUMP          // 跳轉指令
         * 3: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, (byte) 0xFF, // PUSH1 0xFF
                0x56,              // JUMP
                0x00               // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);

        // 驗證跳轉到超出範圍的地址會拋出異常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            evm.run();
        });

        assertTrue(exception.getMessage().contains("Invalid jump destination"));
    }

    @Test
    @DisplayName("測試複雜的跳轉邏輯 - 多個 JUMPDEST")
    void testComplexJumpLogic() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x07    // 推送跳轉目標地址 7
         * 2: JUMP          // 跳轉到第一個 JUMPDEST
         * 3: PUSH1 0x99    // 不應該執行
         * 5: JUMPDEST      // 第二個 JUMPDEST (地址 5)
         * 6: STOP          // 停止執行
         * 7: JUMPDEST      // 第一個 JUMPDEST (地址 7)
         * 8: PUSH1 0x05    // 推送第二個跳轉目標
         * 10: JUMP         // 跳轉到第二個 JUMPDEST
         * 11: PUSH1 0x88   // 不應該執行
         */
        byte[] bytecode = {
                0x60, 0x07,       // PUSH1 0x07
                0x56,             // JUMP
                0x60, (byte) 0x99,       // PUSH1 0x99 (shouldn't execute)
                0x5B,             // JUMPDEST (address 5)
                0x00,             // STOP
                0x5B,             // JUMPDEST (address 7)
                0x60, 0x05,       // PUSH1 0x05
                0x56,             // JUMP
                0x60, (byte) 0x88        // PUSH1 0x88 (shouldn't execute)
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證執行路徑：跳轉到地址7，然後跳轉到地址5並停止
        // 堆疊應該是空的，因為最後執行的是 STOP
        assertEquals(0, evm.getStack().size());
    }

    @Test
    @DisplayName("測試堆疊不足時的 JUMP 指令")
    void testJumpWithStackUnderflow() {
        /*
         * 字節碼結構：
         * 0: JUMP          // 沒有推送地址就直接跳轉
         * 1: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x56,             // JUMP (no address on stack)
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);

        // 驗證堆疊不足時會拋出異常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            evm.run();
        });

        assertTrue(exception.getMessage().contains("Stack underflow"));
    }

    @Test
    @DisplayName("測試堆疊不足時的 JUMPI 指令")
    void testJumpiWithStackUnderflow() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x05    // 只推送一個值（需要兩個）
         * 2: JUMPI         // 條件跳轉需要兩個值
         * 3: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x05,       // PUSH1 0x05 (only one value, need two)
                0x57,             // JUMPI
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);

        // 驗證堆疊不足時會拋出異常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            evm.run();
        });

        assertTrue(exception.getMessage().contains("Stack underflow"));
    }

    @Test
    @DisplayName("測試 JUMPDEST 指令本身不影響執行")
    void testJumpDestDoesNotAffectExecution() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x42    // 推送值 0x42
         * 2: JUMPDEST      // JUMPDEST 不應該影響堆疊
         * 3: PUSH1 0x24    // 推送值 0x24
         * 5: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x42,       // PUSH1 0x42
                0x5B,             // JUMPDEST
                0x60, 0x24,       // PUSH1 0x24
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證 JUMPDEST 不影響堆疊狀態
        assertEquals(0x24, evm.getStack().safePop()); // 最後推送的值
        assertEquals(0x42, evm.getStack().safePop()); // 第一個推送的值
    }

    @Test
    @DisplayName("測試向後跳轉（循環）")
    void testBackwardJump() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x01    // 推送計數器初始值 1
         * 2: JUMPDEST      // 循環開始 (地址 2)
         * 3: DUP1          // 複製計數器
         * 4: PUSH1 0x03    // 推送最大值 3
         * 6: SUB           // 計算差值
         * 7: PUSH1 0x10    // 推送跳出循環的地址 16
         * 9: JUMPI         // 如果計數器達到3則跳出
         * 10: PUSH1 0x01   // 推送增量 1
         * 12: ADD          // 計數器 + 1
         * 13: PUSH1 0x02   // 推送循環開始地址 2
         * 15: JUMP         // 跳回循環開始
         * 16: JUMPDEST     // 循環結束 (地址 16)
         * 17: STOP         // 停止執行
         */
        byte[] bytecode = {
                Opcode.PUSH1.getCode(), 0x01,       // PUSH1 0x01 (counter = 1)
                Opcode.JUMPDEST.getCode(),          // JUMPDEST (loop start, address 2)
                Opcode.DUP1.getCode(),              // DUP1 (duplicate counter)
                Opcode.PUSH1.getCode(), 0x03,       // PUSH1 0x03 (max value)
                Opcode.SUB.getCode(),               // SUB (counter - 3)
                Opcode.PUSH1.getCode(), 0x10,       // PUSH1 0x10 (exit address 16)
                Opcode.JUMPI.getCode(),             // JUMPI (jump if counter >= 3)
                Opcode.PUSH1.getCode(), 0x01,       // PUSH1 0x01 (increment)
                Opcode.ADD.getCode(),               // ADD (counter++)
                Opcode.PUSH1.getCode(), 0x02,       // PUSH1 0x02 (loop start address)
                Opcode.JUMP.getCode(),              // JUMP (back to loop)
                Opcode.JUMPDEST.getCode(),          // JUMPDEST (exit point, address 16)
                Opcode.STOP.getCode()               // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證循環執行後計數器的值
        assertEquals(1, evm.peek());
    }
}
