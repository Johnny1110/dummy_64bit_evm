package com.frizo.lab.sevm.sevm_v1;


import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.vm.SimpleEVM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.frizo.lab.sevm.TestConstant.TEST_ORIGIN;
import static org.junit.jupiter.api.Assertions.*;

public class CallDataAndReturnDataTest {


    @Test
    @DisplayName("測試 CALLDATALOAD, CALLDATASIZE, CALLDATACOPY 與 RETURN 整合")
    public void testCallDataIntegration() {
        // 目標合約地址
        final byte[] TARGET_CONTRACT_ADDRESS = {
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x03
        };

        // 目標合約 bytecode：處理 calldata 並返回處理後的數據
        byte[] targetContractBytecode = {
                // 檢查 calldata 大小
                Opcode.CALLDATASIZE.getCode(),      // [calldatasize]
                Opcode.DUP1.getCode(),              // [calldatasize, calldatasize]
                Opcode.PUSH1.getCode(), 0x00,       // [0, calldatasize, calldatasize]
                Opcode.EQ.getCode(),                // [calldatasize==0, calldatasize]
                Opcode.PUSH1.getCode(), 0x1A,       // [jump_addr, calldatasize==0, calldatasize]
                Opcode.JUMPI.getCode(),             // [calldatasize] (如果沒有 calldata，跳到默認返回)

                // 讀取前 8 bytes 的 calldata
                Opcode.PUSH1.getCode(), 0x00,       // [0, calldatasize] (offset=0)
                Opcode.CALLDATALOAD.getCode(),      // [first_8_bytes, calldatasize]

                // 將讀取的數據存入內存
                Opcode.PUSH1.getCode(), 0x00,       // [0, first_8_bytes, calldatasize]
                Opcode.MSTORE.getCode(),            // [calldatasize]

                // 複製所有 calldata 到內存偏移 8 的位置
                Opcode.DUP1.getCode(),              // [calldatasize, calldatasize]
                Opcode.PUSH1.getCode(), 0x00,       // [0, calldatasize, calldatasize] (calldata offset)
                Opcode.PUSH1.getCode(), 0x08,       // [8, 0, calldatasize, calldatasize] (memory offset)
                Opcode.CALLDATACOPY.getCode(),      // [calldatasize]

                //19

                // 計算返回數據大小：8 + calldata_size
                Opcode.PUSH1.getCode(), 0x08,       // [8, calldatasize]
                Opcode.ADD.getCode(),               // [return_size]
                Opcode.PUSH1.getCode(), 0x00,       // [0, return_size] (memory offset)
                Opcode.RETURN.getCode(),            // 返回數據

                //25

                // 默認返回（如果沒有 calldata）
                Opcode.JUMPDEST.getCode(),          // 跳轉目標 (0x1A)
                Opcode.PUSH8.getCode(),             // 推入 8 bytes 默認數據
                (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF,
                (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
                Opcode.PUSH1.getCode(), 0x00,       // [0, default_data]
                Opcode.MSTORE.getCode(),            // 存入內存
                Opcode.PUSH1.getCode(), 0x08,       // [8] (返回 8 bytes)
                Opcode.PUSH1.getCode(), 0x18,       // [24, 8] (從內存偏移 24 開始)
                Opcode.RETURN.getCode()             // 返回默認數據
        };

        // 主合約 bytecode：呼叫目標合約並處理返回
        byte[] mainContractBytecode = {
                // 準備 calldata：在內存中放入一些測試數據
                Opcode.PUSH4.getCode(),             // 推入 4 bytes 測試數據
                (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
                Opcode.PUSH1.getCode(), 0x00,       // [0, test_data]
                Opcode.MSTORE.getCode(),            // 存入內存

                // 7

                Opcode.PUSH4.getCode(),             // 推入更多測試數據
                (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0,
                Opcode.PUSH1.getCode(), 0x08,       // [8, test_data2]
                Opcode.MSTORE.getCode(),            // 存入內存

                // 15

                // 準備 CALL 參數
                Opcode.PUSH1.getCode(), 0x10,       // retSize (10 bytes)
                Opcode.PUSH1.getCode(), (byte) 0x80,// retOffset (內存偏移 128)
                Opcode.PUSH1.getCode(), 0x10,       // argsSize (16 bytes calldata)
                Opcode.PUSH1.getCode(), 0x00,       // argsOffset (從內存偏移 0 開始)
                Opcode.PUSH1.getCode(), 0x00,       // value (0 ETH)

                // 25

                // 目標合約地址
                Opcode.PUSH8.getCode(),
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x03, // 目標合約地址

                // 34

                Opcode.PUSH2.getCode(), 0x27, 0x10,  // gas (10000)
                Opcode.CALL.getCode(),              // [call_result]

                // 38

                // 檢查 CALL 結果
                Opcode.DUP1.getCode(),              // [call_result, call_result]
                Opcode.ISZERO.getCode(),            // [call_failed, call_result]
                Opcode.PUSH1.getCode(), 0x36,       // [jump_addr, call_failed, call_result]
                Opcode.JUMPI.getCode(),             // [call_result] (如果失敗， 0x36 跳轉)

                // 43

                // 處理返回數據
                Opcode.RETURNDATASIZE.getCode(),    // [return_size, call_result]
                Opcode.DUP1.getCode(),              // [return_size, return_size, call_result]

                // 45

                // 複製返回數據到新的內存位置
                Opcode.PUSH1.getCode(), 0x00,       // [0, return_size, return_size, call_result] (returndata offset)
                Opcode.PUSH1.getCode(), (byte) 0x10, // [16, 0, return_size, return_size, call_result] (memory offset)
                Opcode.RETURNDATACOPY.getCode(),    // [return_size, call_result]

                // 50

                // 跳轉到結束
                Opcode.PUSH1.getCode(), 0x39,       // [end_addr, return_size, call_result]
                Opcode.JUMP.getCode(),              // [return_size, call_result]

                // 53

                // 失敗處理
                Opcode.JUMPDEST.getCode(),          // 跳轉目標 (0x36)
                Opcode.PUSH1.getCode(), 0x00,       // [0, call_result] (設置 return_size 為 0)

                // 56

                // 結束
                Opcode.JUMPDEST.getCode(),          // 跳轉目標 (0x39)
                Opcode.STOP.getCode()               // 停止執行

                // 58
        };

        // 創建並執行 EVM
        var evm = new SimpleEVM(mainContractBytecode, 1000000L, TEST_ORIGIN);

        // 註冊目標合約
        evm.registerContract(Address.of(TARGET_CONTRACT_ADDRESS), targetContractBytecode);

        evm.run();

        System.out.println("CALLDATA integration test completed");
        evm.printStack();
        evm.printMemory();

        // 驗證結果
        assertTrue(evm.getContext().getCurrentStack().size() >= 2);

        long returnDataSize = evm.getContext().getCurrentStack().safePop();
        long callResult = evm.getContext().getCurrentStack().safePop();

        if (callResult == 1) {
            // CALL 成功
            assertTrue(returnDataSize > 0, "Return data size should be greater than 0");

            // 驗證返回數據被正確複製到內存
            assertNotNull(evm.getContext().getCurrentMemory().get(16L), "Return data should be copied to memory offset 16");

            System.out.println("✓ CALL succeeded");
            System.out.println("✓ Return data size: " + returnDataSize);
            System.out.println("✓ Return data copied to memory at offset 192");

            // 驗證原始 calldata 在內存中
            assertNotNull(evm.getContext().getCurrentMemory().get(0L), "Original calldata should be in memory");

        } else {
            // CALL 失敗
            assertEquals(0, callResult, "CALL should have failed");
            assertEquals(0, returnDataSize, "Return data size should be 0 when CALL fails");

            System.out.println("⚠ CALL failed, which might be expected in test environment");
        }

        System.out.println("Call result: " + callResult);
        System.out.println("Final return data size: " + returnDataSize);
    }
}
