package com.frizo.lab.sevm;

import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.utils.NumUtils;
import com.frizo.lab.sevm.vm.SimpleEVM;
import com.frizo.lab.sevm.op.Opcode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CallReturnTest {

    private SimpleEVM evm;
    private static final String TEST_ORIGIN = "0x123456789A";

    @BeforeEach
    void setUp() {
        // 每個測試前的初始化
    }

    @Test
    @DisplayName("測試內部呼叫 (ICALL)")
    public void testInternalCall() {
        // 測試程式碼：
        // PUSH1 10 (gas)
        // PUSH1 8  (jump address)
        // ICALL
        // STOP
        // JUMPDEST (address 8)
        // PUSH1 42
        // PUSH1 0
        // MSTORE
        // PUSH1 32
        // PUSH1 0
        // RETURN

        byte[] bytecode = {
                Opcode.PUSH4.getCode(), (byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0xFF,  // PUSH1 255 (gas)
                Opcode.PUSH4.getCode(), 0x00,  0x00,  0x00,  0x0C,  // PUSH1 12 (address)
                Opcode.ICALL.getCode(),         // ICALL
                Opcode.STOP.getCode(),          // STOP
                Opcode.JUMPDEST.getCode(),      // JUMPDEST (address 12)
                Opcode.PUSH1.getCode(), 0x2A,  // PUSH1 42
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0
                Opcode.MSTORE.getCode(),        // MSTORE
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x01,  // PUSH1 1
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x00,  // PUSH1 0
                Opcode.RETURN.getCode()         // RETURN
        };

        evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        System.out.println("Internal call test completed");
        evm.printStack();
        evm.printMemory();

        System.out.println("Return data: " + Arrays.toString(evm.getContext().getCurrentFrame().getReturnData()));


        // 驗證執行結果
        assertFalse(evm.isRunning());
        evm.getContext().getCallStack().printStack();
        assertEquals(1, evm.getContext().getCallStack().size());
    }

    @Test
    @DisplayName("Test code")
    void testCode() {
        int pc = 0;
        int codeCount = 1;
        byte[] bytecode = {
                (byte) 0x1111, //4369
        };


        byte[] result = new byte[codeCount];

        for (int i = 0; i < codeCount; i++) {
            if (pc + i < codeCount) {
                result[i] = bytecode[pc + i];
            } else {
                result[i] = 0;
            }
        }

        System.out.println("Result: " + NumUtils.bytesToInt(result));
    }

    @Test
    @DisplayName("測試外部呼叫 (CALL)")
    public void testExternalCall() {
        // 測試程式碼：
        // PUSH1 100 (retSize)
        // PUSH1 0   (retOffset)
        // PUSH1 32  (argsSize)
        // PUSH1 0   (argsOffset)
        // PUSH1 0   (value)
        // PUSH1 0x1111 (address)
        // PUSH1 500 (gas)
        // CALL
        // STOP

        byte[] bytecode = {
                Opcode.PUSH1.getCode(), 0x01,  // PUSH1 1 (retSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (retOffset)
                Opcode.PUSH1.getCode(), 0x01,  // PUSH1 1 (argsSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsOffset)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (value)
                Opcode.PUSH1.getCode(), (byte)0x1111, // PUSH1 0x1111 (address)
                Opcode.PUSH1.getCode(), (byte)0xFF, // PUSH1 500 (gas)
                Opcode.CALL.getCode(),          // CALL
                Opcode.STOP.getCode()           // STOP
        };

        evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        System.out.println("External call test completed");
        evm.printStack();
        evm.printMemory();

        // 驗證呼叫成功（堆疊頂部應該是 1）
        assertEquals(1, evm.getContext().getStack().peek().intValue());
    }

    @Test
    @DisplayName("測試靜態呼叫 (STATICCALL)")
    public void testStaticCall() {
        // 測試程式碼：
        // PUSH1 32  (retSize)
        // PUSH1 0   (retOffset)
        // PUSH1 0   (argsSize)
        // PUSH1 0   (argsOffset)
        // PUSH1 0x2222 (address)
        // PUSH1 300 (gas)
        // STATICCALL
        // STOP

        byte[] bytecode = {
                Opcode.PUSH1.getCode(), 0x20,  // PUSH1 32 (retSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (retOffset)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsOffset)
                Opcode.PUSH2.getCode(), 0x22, 0x22, // PUSH2 0x2222 (address)
                Opcode.PUSH2.getCode(), 0x01, 0x2C, // PUSH2 300 (gas)
                Opcode.STATICCALL.getCode(),    // STATICCALL
                Opcode.STOP.getCode()           // STOP
        };

        evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        System.out.println("Static call test completed");
        evm.printStack();

        // 驗證靜態呼叫成功
        assertEquals(1, evm.getContext().getStack().peek().intValue());
    }

    @Test
    @DisplayName("測試委託呼叫 (DELEGATECALL)")
    public void testDelegateCall() {
        // 測試程式碼：
        // PUSH1 32  (retSize)
        // PUSH1 0   (retOffset)
        // PUSH1 0   (argsSize)
        // PUSH1 0   (argsOffset)
        // PUSH1 0x3333 (address)
        // PUSH1 400 (gas)
        // DELEGATECALL
        // STOP

        byte[] bytecode = {
                Opcode.PUSH1.getCode(), 0x20,  // PUSH1 32 (retSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (retOffset)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsSize)
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsOffset)
                Opcode.PUSH2.getCode(), 0x33, 0x33, // PUSH2 0x3333 (address)
                Opcode.PUSH2.getCode(), 0x01, (byte) 0x90, // PUSH2 400 (gas)
                Opcode.DELEGATECALL.getCode(),  // DELEGATECALL
                Opcode.STOP.getCode()           // STOP
        };

        evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        System.out.println("Delegate call test completed");
        evm.printStack();

        // 驗證委託呼叫成功
        assertEquals(1, evm.getContext().getStack().peek().intValue());
    }

    @Test
    @DisplayName("測試正常返回 (RETURN)")
    public void testNormalReturn() {
        // 測試程式碼：
        // PUSH1 42  (要返回的值)
        // PUSH1 0   (memory offset)
        // MSTORE    (將值存入記憶體)
        // PUSH1 32  (return size)
        // PUSH1 0   (return offset)
        // RETURN

        byte[] bytecode = {
                Opcode.PUSH1.getCode(), 0x2A,  // PUSH1 42
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0
                Opcode.MSTORE.getCode(),        // MSTORE
                Opcode.PUSH1.getCode(), 0x20,  // PUSH1 32
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0
                Opcode.RETURN.getCode()         // RETURN
        };

        evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        System.out.println("Normal return test completed");
        evm.printMemory();

        // 驗證執行已停止
        assertFalse(evm.getContext().isRunning());

        // 驗證返回資料
        CallFrame frame = evm.getContext().getCurrentFrame();
        assertTrue(frame.isSuccess());
        assertFalse(frame.isReverted());
        assertTrue(frame.getReturnData().length > 0);
    }

//    @Test
//    @DisplayName("測試回滾 (REVERT)")
//    public void testRevert() {
//        // 測試程式碼：
//        // PUSH1 69  (字元 'E')
//        // PUSH1 0   (memory offset)
//        // MSTORE8   (存入記憶體)
//        // PUSH1 114 (字元 'r')
//        // PUSH1 1   (memory offset)
//        // MSTORE8
//        // PUSH1 114 (字元 'r')
//        // PUSH1 2   (memory offset)
//        // MSTORE8
//        // PUSH1 111 (字元 'o')
//        // PUSH1 3   (memory offset)
//        // MSTORE8
//        // PUSH1 114 (字元 'r')
//        // PUSH1 4   (memory offset)
//        // MSTORE8
//        // PUSH1 5   (revert size)
//        // PUSH1 0   (revert offset)
//        // REVERT
//
//        byte[] bytecode = {
//                Opcode.PUSH1.getCode(), 0x45,  // PUSH1 69 ('E')
//                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0
//                Opcode.MSTORE8.getCode(),       // MSTORE8
//                Opcode.PUSH1.getCode(), 0x72,  // PUSH1 114 ('r')
//                Opcode.PUSH1.getCode(), 0x01,  // PUSH1 1
//                Opcode.MSTORE8.getCode(),       // MSTORE8
//                Opcode.PUSH1.getCode(), 0x72,  // PUSH1 114 ('r')
//                Opcode.PUSH1.getCode(), 0x02,  // PUSH1 2
//                Opcode.MSTORE8.getCode(),       // MSTORE8
//                Opcode.PUSH1.getCode(), 0x6F,  // PUSH1 111 ('o')
//                Opcode.PUSH1.getCode(), 0x03,  // PUSH1 3
//                Opcode.MSTORE8.getCode(),       // MSTORE8
//                Opcode.PUSH1.getCode(), 0x72,  // PUSH1 114 ('r')
//                Opcode.PUSH1.getCode(), 0x04,  // PUSH1 4
//                Opcode.MSTORE8.getCode(),       // MSTORE8
//                Opcode.PUSH1.getCode(), 0x05,  // PUSH1 5 (size)
//                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (offset)
//                Opcode.REVERT.getCode()         // REVERT
//        };
//
//        evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
//        evm.run();
//
//        System.out.println("Revert test completed");
//        evm.printMemory();
//
//        // 驗證執行已停止且回滾
//        assertFalse(evm.getContext().isRunning());
//
//        CallFrame frame = evm.getContext().getCurrentFrame();
//        assertFalse(frame.isSuccess());
//        assertTrue(frame.isReverted());
//        assertNotNull(frame.getRevertReason());
//
//        System.out.println("Revert reason: " + frame.getRevertReason());
//    }
//
//    @Test
//    @DisplayName("測試呼叫棧深度限制")
//    public void testCallStackDepthLimit() {
//        // 創建一個會導致深度呼叫的程式碼
//        byte[] bytecode = new byte[100];
//        int pos = 0;
//
//        // 創建一個遞歸呼叫的程式碼
//        for (int i = 0; i < 10; i++) {
//            bytecode[pos++] = Opcode.PUSH1.getCode();
//            bytecode[pos++] = 0x0A; // gas
//            bytecode[pos++] = Opcode.PUSH1.getCode();
//            bytecode[pos++] = 0x00; // address (自己)
//            bytecode[pos++] = Opcode.PUSH1.getCode();
//            bytecode[pos++] = 0x00; // value
//            bytecode[pos++] = Opcode.PUSH1.getCode();
//            bytecode[pos++] = 0x00; // argsOffset
//            bytecode[pos++] = Opcode.PUSH1.getCode();
//            bytecode[pos++] = 0x00; // argsSize
//            bytecode[pos++] = Opcode.PUSH1.getCode();
//            bytecode[pos++] = 0x00; // retOffset
//            bytecode[pos++] = Opcode.PUSH1.getCode();
//            bytecode[pos++] = 0x00; // retSize
//            bytecode[pos++] = Opcode.CALL.getCode();
//            bytecode[pos++] = Opcode.POP.getCode(); // 移除返回值
//        }
//
//        bytecode[pos++] = Opcode.STOP.getCode();
//
//        evm = new SimpleEVM(bytecode, 10000, TEST_ORIGIN);
//
//        // 這應該會因為呼叫棧過深而失敗
//        assertThrows(RuntimeException.class, () -> evm.run());
//    }
//
//    @Test
//    @DisplayName("測試 Gas 不足的呼叫")
//    public void testOutOfGasCall() {
//        // 測試程式碼：用很少的 gas 進行呼叫
//        byte[] bytecode = {
//                Opcode.PUSH1.getCode(), 0x20,  // PUSH1 32 (retSize)
//                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (retOffset)
//                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsSize)
//                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (argsOffset)
//                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (value)
//                Opcode.PUSH1.getCode(), 0x01,  // PUSH1 1 (address)
//                Opcode.PUSH1.getCode(), 0x01,  // PUSH1 1 (very low gas)
//                Opcode.CALL.getCode(),          // CALL
//                Opcode.STOP.getCode()           // STOP
//        };
//
//        evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
//        evm.run();
//
//        System.out.println("Out of gas call test completed");
//        evm.printStack();
//
//        // 驗證呼叫失敗（堆疊頂部應該是 0）
//        assertEquals(0, evm.getContext().getStack().peek().intValue());
//    }
//
//    @Test
//    @DisplayName("測試多層呼叫")
//    public void testNestedCalls() {
//        // 測試多層呼叫的情況
//        byte[] bytecode = {
//                // 第一層呼叫
//                Opcode.PUSH1.getCode(), 0x20,  // retSize
//                Opcode.PUSH1.getCode(), 0x00,  // retOffset
//                Opcode.PUSH1.getCode(), 0x00,  // argsSize
//                Opcode.PUSH1.getCode(), 0x00,  // argsOffset
//                Opcode.PUSH1.getCode(), 0x00,  // value
//                Opcode.PUSH1.getCode(), 0x01,  // address
//                Opcode.PUSH1.getCode(), 0x64,  // gas (100)
//                Opcode.CALL.getCode(),          // CALL
//
//                // 檢查第一層呼叫結果
//                Opcode.ISZERO.getCode(),        // 檢查是否為 0
//                Opcode.PUSH1.getCode(), 0x20,  // 如果失敗跳轉到位置 32
//                Opcode.JUMPI.getCode(),         // 條件跳轉
//
//                // 第二層呼叫
//                Opcode.PUSH1.getCode(), 0x20,  // retSize
//                Opcode.PUSH1.getCode(), 0x00,  // retOffset
//                Opcode.PUSH1.getCode(), 0x00,  // argsSize
//                Opcode.PUSH1.getCode(), 0x00,  // argsOffset
//                Opcode.PUSH1.getCode(), 0x00,  // value
//                Opcode.PUSH1.getCode(), 0x02,  // address
//                Opcode.PUSH1.getCode(), 0x64,  // gas (100)
//                Opcode.CALL.getCode(),          // CALL
//
//                Opcode.JUMPDEST.getCode(),      // 目標位置 (32)
//                Opcode.STOP.getCode()           // STOP
//        };
//
//        evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
//        evm.run();
//
//        System.out.println("Nested calls test completed");
//        evm.printStack();
//
//        // 驗證執行完成
//        assertFalse(evm.getContext().isRunning());
//    }
//
//    @Test
//    @DisplayName("測試呼叫上下文保存")
//    public void testCallContextPreservation() {
//        // 測試呼叫前後上下文是否正確保存和恢復
//        byte[] bytecode = {
//                // 在堆疊放入一些測試值
//                Opcode.PUSH1.getCode(), 0x11,  // 測試值 1
//                Opcode.PUSH1.getCode(), 0x22,  // 測試值 2
//                Opcode.PUSH1.getCode(), 0x33,  // 測試值 3
//
//                // 進行呼叫
//                Opcode.PUSH1.getCode(), 0x00,  // retSize
//                Opcode.PUSH1.getCode(), 0x00,  // retOffset
//                Opcode.PUSH1.getCode(), 0x00,  // argsSize
//                Opcode.PUSH1.getCode(), 0x00,  // argsOffset
//                Opcode.PUSH1.getCode(), 0x00,  // value
//                Opcode.PUSH1.getCode(), 0x01,  // address
//                Opcode.PUSH1.getCode(), 0x64,  // gas
//                Opcode.CALL.getCode(),          // CALL
//
//                // 呼叫後驗證堆疊
//                Opcode.POP.getCode(),           // 移除呼叫結果
//                Opcode.STOP.getCode()           // STOP
//        };
//
//        evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
//        evm.run();
//
//        System.out.println("Call context preservation test completed");
//        evm.printStack();
//
//        // 驗證原始值仍在堆疊中
//        assertEquals(3, evm.getContext().getStack().size());
//        assertEquals(0x33, evm.getContext().getStack().get(2).intValue());
//        assertEquals(0x22, evm.getContext().getStack().get(1).intValue());
//        assertEquals(0x11, evm.getContext().getStack().get(0).intValue());
//    }
}