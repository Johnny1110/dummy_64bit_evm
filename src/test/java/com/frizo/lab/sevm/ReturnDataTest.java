package com.frizo.lab.sevm;

import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.vm.SimpleEVM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReturnDataTest {

    private static final String TEST_ORIGIN = "0x123456789A";

    @Test
    @DisplayName("測試 RETURNDATASIZE 操作")
    public void testReturnDataSize() {
        byte[] bytecode = {
                // 先進行一個外部呼叫來產生返回數據
                Opcode.PUSH1.getCode(), 0x65,  // PUSH1 101 (MSTORE value)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (MSTORE offset)
                Opcode.MSTORE.getCode(),        // MSTORE
                Opcode.PUSH1.getCode(), 0x04,  // PUSH1 4 (retSize)
                Opcode.PUSH1.getCode(), 0x01,  // PUSH1 1 (retOffset)
                Opcode.PUSH1.getCode(), 0x08,  // PUSH1 8 (argsSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsOffset)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (value)
                Opcode.PUSH4.getCode(), (byte) 0x1C, (byte) 0x3D, (byte) 0xA6, (byte) 0x18, // PUSH4 address
                Opcode.PUSH1.getCode(), (byte) 0x64, // PUSH1 100 (gas)
                Opcode.CALL.getCode(),          // CALL

                // 測試 RETURNDATASIZE
                Opcode.RETURNDATASIZE.getCode(), // RETURNDATASIZE - 應該將返回數據大小推入堆疊
                Opcode.STOP.getCode()           // STOP
        };

        var evm = new SimpleEVM(bytecode, 1000000L, TEST_ORIGIN);
        evm.run();

        System.out.println("RETURNDATASIZE test completed");
        evm.printStack();
        evm.printMemory();

        // 驗證堆疊狀態
        // 堆疊頂部應該是返回數據的大小
        // 第二個元素應該是 CALL 的返回值 (1 表示成功)
        assertEquals(2, evm.getContext().getStack().size()); // 應該有兩個元素

        long returnDataSize = evm.getContext().getStack().safePop();
        long callResult = evm.getContext().getStack().safePop();

        assertEquals(1, callResult); // CALL 成功
        assertTrue(returnDataSize > 0); // 返回數據大小應該大於 0

        System.out.println("Return data size: " + returnDataSize);
        System.out.println("Call result: " + callResult);
    }

    @Test
    @DisplayName("測試 RETURNDATACOPY 操作")
    public void testReturnDataCopy() {
        byte[] bytecode = {
                // 先進行一個外部呼叫來產生返回數據
                Opcode.PUSH1.getCode(), 0x65,  // PUSH1 101 (MSTORE value)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (MSTORE offset)
                Opcode.MSTORE.getCode(),        // MSTORE
                Opcode.PUSH1.getCode(), 0x08,  // PUSH1 8 (retSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (retOffset)
                Opcode.PUSH1.getCode(), 0x08,  // PUSH1 8 (argsSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsOffset)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (value)
                Opcode.PUSH4.getCode(), (byte) 0x1C, (byte) 0x3D, (byte) 0xA6, (byte) 0x18, // PUSH4 address
                Opcode.PUSH1.getCode(), (byte) 0x64, // PUSH1 100 (gas)
                Opcode.CALL.getCode(),          // CALL

                Opcode.RETURNDATASIZE.getCode(), // RETURNDATASIZE - 將返回數據大小推入堆疊
                Opcode.DUP1.getCode(),          // DUP1 - 複製數據大小值
                // 測試 RETURNDATACOPY
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (returnDataOffset - 從返回數據的偏移量)
                Opcode.PUSH1.getCode(), 0x40,  // PUSH1 64 (memoryOffset - 目標記憶體位置)
                Opcode.RETURNDATACOPY.getCode(), // RETURNDATACOPY
                Opcode.STOP.getCode()           // STOP
        };

        var evm = new SimpleEVM(bytecode, 1000000L, TEST_ORIGIN);
        evm.run();

        System.out.println("RETURNDATACOPY test completed");
        evm.printStack();
        evm.printMemory();

        // 驗證堆疊狀態 - 應該只剩下 return size + CALL 的返回值
        assertEquals(2, evm.getContext().getStack().size());
        assertEquals(8, evm.getContext().getStack().safePop()); // 返回值大小
        assertEquals(1, evm.getContext().getStack().safePop()); // CALL 成功

        // 驗證記憶體中是否正確複製了返回數據
        // 檢查記憶體位置 0x40 (64) 處是否有數據
        assertNotNull(evm.getContext().getMemory().get(64L));

        System.out.println("Return data copied to memory at offset 64");
        System.out.println("Memory content at offset 64: " + evm.getContext().getMemory().get(64L));
    }

    @Test
    @DisplayName("測試 RETURNDATASIZE 和 RETURNDATACOPY 組合使用")
    public void testReturnDataSizeAndCopy() {
        byte[] bytecode = {
                // 先進行一個外部呼叫來產生返回數據
                Opcode.PUSH1.getCode(), 0x65,  // PUSH1 101 (MSTORE value)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (MSTORE offset)
                Opcode.MSTORE.getCode(),        // MSTORE
                Opcode.PUSH1.getCode(), 0x10,  // PUSH1 16 (retSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (retOffset)
                Opcode.PUSH1.getCode(), 0x08,  // PUSH1 8 (argsSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsOffset)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (value)

                Opcode.PUSH8.getCode(),
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x02, // PUSH8 address: 這個地址會 RETURN 8 bytes 的數據

                Opcode.PUSH1.getCode(), (byte) 0x64, // PUSH1 100 (gas)
                Opcode.CALL.getCode(),          // CALL

                // 使用 RETURNDATASIZE 獲取返回數據大小
                Opcode.RETURNDATASIZE.getCode(), // RETURNDATASIZE - 將返回數據大小推入堆疊
                Opcode.DUP1.getCode(),          // DUP1 - 複製大小值

                // 使用 RETURNDATACOPY 複製完整的返回數據
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (returnDataOffset)
                Opcode.PUSH1.getCode(), (byte)0x80,  // PUSH1 128 (memoryOffset)
                Opcode.RETURNDATACOPY.getCode(), // RETURNDATACOPY

                Opcode.STOP.getCode()           // STOP
        };

        var evm = new SimpleEVM(bytecode, 1000000L, TEST_ORIGIN);
        evm.run();

        System.out.println("Combined RETURNDATASIZE and RETURNDATACOPY test completed");
        evm.printStack();
        evm.printMemory();

        // 驗證堆疊狀態
        assertEquals(2, evm.getContext().getStack().size());

        long returnDataSize = evm.getContext().getStack().safePop();
        long callResult = evm.getContext().getStack().safePop();

        assertEquals(1, callResult); // CALL 成功
        assertTrue(returnDataSize > 0); // 返回數據大小應該大於 0

        // 驗證記憶體中是否正確複製了返回數據
        assertNotNull(evm.getContext().getMemory().get(128L));

        System.out.println("Return data size: " + returnDataSize);
        System.out.println("Call result: " + callResult);
        System.out.println("Return data copied to memory at offset 128");
    }

    @Test
    @DisplayName("測試空返回數據的 RETURNDATASIZE")
    public void testEmptyReturnDataSize() {
        byte[] bytecode = {
                // 進行一個沒有返回數據的外部呼叫
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (retSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (retOffset)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsOffset)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (value)

                Opcode.PUSH8.getCode(),
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, // PUSH8 address of returnNothingContract

                Opcode.PUSH1.getCode(), (byte) 0x64, // PUSH1 100 (gas)
                Opcode.CALL.getCode(),          // CALL

                // 測試 RETURNDATASIZE
                Opcode.RETURNDATASIZE.getCode(), // RETURNDATASIZE - 應該返回 0
                Opcode.STOP.getCode()           // STOP
        };

        var evm = new SimpleEVM(bytecode, 1000000L, TEST_ORIGIN);
        evm.run();

        System.out.println("Empty return data test completed");
        evm.printStack();

        // 驗證堆疊狀態
        assertEquals(2, evm.getContext().getStack().size());

        long returnDataSize = evm.getContext().getStack().safePop();
        long callResult = evm.getContext().getStack().safePop();

        assertEquals(1, callResult); // CALL 成功
        assertEquals(0, returnDataSize); // 沒有返回數據，大小應該為 0

        System.out.println("Empty return data size: " + returnDataSize);
    }

}
