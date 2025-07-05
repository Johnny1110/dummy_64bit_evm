package com.frizo.lab.sevm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class DupInstructionTest {

    private SimpleEVM evm;
    private static final int INITIAL_GAS = 1000000;

    @BeforeEach
    void setUp() {
        // 每個測試前重置 EVM
    }

    @Test
    @DisplayName("測試 DUP1 指令 - 複製堆疊頂部元素")
    void testDup1() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x42    // 推送值 0x42
         * 2: DUP1          // 複製堆疊頂部元素
         * 3: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x42,       // PUSH1 0x42
                (byte)0x80,             // DUP1
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證 DUP1 後堆疊應該有兩個相同的元素
        assertEquals(2, evm.getStack().size());
        assertEquals(0x42, evm.getStack().safePop()); // 複製的元素
        assertEquals(0x42, evm.getStack().safePop()); // 原始元素
    }

    @Test
    @DisplayName("測試 DUP2 指令 - 複製堆疊第二個元素")
    void testDup2() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x11    // 推送值 0x11 (將成為第二個元素)
         * 2: PUSH1 0x22    // 推送值 0x22 (頂部元素)
         * 4: DUP2          // 複製第二個元素 (0x11)
         * 5: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x11,       // PUSH1 0x11
                0x60, 0x22,       // PUSH1 0x22
                (byte)0x81,             // DUP2
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證 DUP2 後堆疊狀態
        assertEquals(3, evm.getStack().size());
        assertEquals(0x11, evm.getStack().safePop()); // 複製的第二個元素
        assertEquals(0x22, evm.getStack().safePop()); // 原始頂部元素
        assertEquals(0x11, evm.getStack().safePop()); // 原始第二個元素
    }

    @Test
    @DisplayName("測試 DUP3 指令 - 複製堆疊第三個元素")
    void testDup3() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x11    // 推送值 0x11 (將成為第三個元素)
         * 2: PUSH1 0x22    // 推送值 0x22 (將成為第二個元素)
         * 4: PUSH1 0x33    // 推送值 0x33 (頂部元素)
         * 6: DUP3          // 複製第三個元素 (0x11)
         * 7: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x11,       // PUSH1 0x11
                0x60, 0x22,       // PUSH1 0x22
                0x60, 0x33,       // PUSH1 0x33
                (byte)0x82,             // DUP3
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證 DUP3 後堆疊狀態
        assertEquals(4, evm.getStack().size());
        assertEquals(0x11, evm.getStack().safePop()); // 複製的第三個元素
        assertEquals(0x33, evm.getStack().safePop()); // 原始頂部元素
        assertEquals(0x22, evm.getStack().safePop()); // 原始第二個元素
        assertEquals(0x11, evm.getStack().safePop()); // 原始第三個元素
    }

    @Test
    @DisplayName("測試 DUP16 指令 - 複製堆疊第16個元素")
    void testDup16() {
        /*
         * 創建一個有16個元素的堆疊，然後使用 DUP16 複製第16個元素
         */
        byte[] bytecode = createBytecodeWithNPushes(16);
        // 添加 DUP16 指令
        byte[] fullBytecode = new byte[bytecode.length + 2];
        System.arraycopy(bytecode, 0, fullBytecode, 0, bytecode.length);
        fullBytecode[bytecode.length] = (byte) 0x8F;     // DUP16
        fullBytecode[bytecode.length + 1] = 0x00;        // STOP

        evm = new SimpleEVM(fullBytecode, INITIAL_GAS);
        evm.run();

        // 驗證 DUP16 後堆疊狀態
        assertEquals(17, evm.getStack().size());
        assertEquals(0x01, evm.getStack().peek()); // 複製的第16個元素應該是 0x01
    }

    @Test
    @DisplayName("測試 DUP1 堆疊不足 - 應該拋出異常")
    void testDup1StackUnderflow() {
        /*
         * 字節碼結構：
         * 0: DUP1          // 沒有元素可複製
         * 1: STOP          // 停止執行
         */
        byte[] bytecode = {
                (byte)0x80,             // DUP1 (no elements on stack)
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
    @DisplayName("測試 DUP2 堆疊不足 - 應該拋出異常")
    void testDup2StackUnderflow() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x42    // 只有一個元素，但 DUP2 需要至少兩個
         * 2: DUP2          // 堆疊不足
         * 3: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x42,       // PUSH1 0x42
                (byte)0x81,             // DUP2 (need at least 2 elements)
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);

        // 驗證堆疊不足時會拋出異常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            evm.run();
        });

        assertTrue(exception.getMessage().contains("Stack underflow: not enough items on the stack"));
    }

    @Test
    @DisplayName("測試 DUP16 堆疊不足 - 應該拋出異常")
    void testDup16StackUnderflow() {
        /*
         * 創建一個只有15個元素的堆疊，然後使用 DUP16
         */
        byte[] bytecode = createBytecodeWithNPushes(15);
        // 添加 DUP16 指令
        byte[] fullBytecode = new byte[bytecode.length + 2];
        System.arraycopy(bytecode, 0, fullBytecode, 0, bytecode.length);
        fullBytecode[bytecode.length] = (byte) 0x8F;     // DUP16
        fullBytecode[bytecode.length + 1] = 0x00;        // STOP

        evm = new SimpleEVM(fullBytecode, INITIAL_GAS);

        // 驗證堆疊不足時會拋出異常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            evm.run();
        });

        assertTrue(exception.getMessage().contains("Stack underflow"));
    }

    @Test
    @DisplayName("測試 DUP 指令與算術運算結合")
    void testDupWithArithmetic() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x05    // 推送值 5
         * 2: DUP1          // 複製值 5
         * 3: DUP1          // 再次複製值 5
         * 4: ADD           // 5 + 5 = 10
         * 5: MUL           // 10 * 5 = 50
         * 6: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x05,       // PUSH1 0x05
                (byte)0x80,             // DUP1
                (byte)0x80,             // DUP1
                0x01,             // ADD
                0x02,             // MUL
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證計算結果
        assertEquals(50, evm.peek());
    }

    @Test
    @DisplayName("測試連續 DUP 操作")
    void testConsecutiveDupOperations() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x11    // 推送值 0x11
         * 2: PUSH1 0x22    // 推送值 0x22
         * 4: DUP2          // 複製 0x11
         * 5: DUP2          // 複製 0x22
         * 6: DUP4          // 複製 0x11
         * 7: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x11,       // PUSH1 0x11
                0x60, 0x22,       // PUSH1 0x22
                (byte)0x81,             // DUP2 (copy 0x11)
                (byte)0x81,             // DUP2 (copy 0x22)
                (byte)0x83,             // DUP4 (copy 0x11)
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證堆疊狀態
        assertEquals(5, evm.getStack().size());
        assertEquals(0x11, evm.getStack().safePop()); // DUP4 結果
        assertEquals(0x22, evm.getStack().safePop()); // DUP2 結果
        assertEquals(0x11, evm.getStack().safePop()); // DUP2 結果
        assertEquals(0x22, evm.getStack().safePop()); // 原始 0x22
        assertEquals(0x11, evm.getStack().safePop()); // 原始 0x11
    }

    @Test
    @DisplayName("測試 DUP 指令的 Gas 消耗")
    void testDupGasConsumption() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x42    // 推送值 0x42
         * 2: DUP1          // 複製堆疊頂部元素
         * 3: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x42,       // PUSH1 0x42
                (byte)0x80,             // DUP1
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        int gasBeforeRun = evm.getGasRemaining();
        evm.run();
        int gasAfterRun = evm.getGasRemaining();

        // 驗證 Gas 被消耗
        assertTrue(gasAfterRun < gasBeforeRun);

        // 驗證執行成功
        assertEquals(0x42, evm.peek());
    }

    @Test
    @DisplayName("測試堆疊達到上限時的 DUP 操作")
    void testDupAtStackLimit() {
        /*
         * 這個測試需要創建一個接近堆疊上限的場景
         * 然後測試 DUP 操作是否會導致堆疊溢出
         */

        // 創建一個會導致堆疊溢出的字節碼
        // 假設堆疊上限是 1024，我們先推送 1024 個元素
        int stackLimit = 1024;
        byte[] bytecode = createBytecodeWithNPushes(stackLimit);

        // 添加 DUP1 指令試圖超出堆疊限制
        byte[] fullBytecode = new byte[bytecode.length + 2];
        System.arraycopy(bytecode, 0, fullBytecode, 0, bytecode.length);
        fullBytecode[bytecode.length] = (byte) 0x80;     // DUP1
        fullBytecode[bytecode.length + 1] = 0x00;        // STOP

        evm = new SimpleEVM(fullBytecode, INITIAL_GAS);

        // 驗證堆疊溢出時會拋出異常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            evm.run();
        });

        assertTrue(exception.getMessage().contains("Stack overflow"));
    }

    @Test
    @DisplayName("測試 DUP 指令與 SWAP 指令結合")
    void testDupWithSwap() {
        /*
         * 字節碼結構：
         * 0: PUSH1 0x11    // 推送值 0x11
         * 2: PUSH1 0x22    // 推送值 0x22
         * 4: DUP2          // 複製 0x11
         * 5: SWAP1         // 交換頂部兩個元素
         * 6: STOP          // 停止執行
         */
        byte[] bytecode = {
                0x60, 0x11,       // PUSH1 0x11
                0x60, 0x22,       // PUSH1 0x22
                (byte)0x81,             // DUP2
                (byte)0x90,             // SWAP1
                0x00              // STOP
        };

        evm = new SimpleEVM(bytecode, INITIAL_GAS);
        evm.run();

        // 驗證最終堆疊狀態
        assertEquals(3, evm.getStack().size());
        assertEquals(0x22, evm.getStack().safePop()); // SWAP 後的頂部
        assertEquals(0x11, evm.getStack().safePop()); // SWAP 後的第二個
        assertEquals(0x11, evm.getStack().safePop()); // 原始底部
    }

    /**
     * 輔助方法：創建包含 N 個 PUSH 指令的字節碼
     */
    private byte[] createBytecodeWithNPushes(int n) {
        byte[] bytecode = new byte[n * 2]; // 每個 PUSH1 需要 2 個字節
        for (int i = 0; i < n; i++) {
            bytecode[i * 2] = 0x60;         // PUSH1
            bytecode[i * 2 + 1] = (byte) (i + 1); // 值從 1 開始
        }
        return bytecode;
    }
}