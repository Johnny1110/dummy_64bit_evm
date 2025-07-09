package com.frizo.lab.sevm.sevm_v2;

import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.utils.NumUtils;
import com.frizo.lab.sevm.vm.EVMResult;
import com.frizo.lab.sevm.vm.SEVM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneralSevmTest {

    @Test
    public void testGeneralSevm() {
        SEVM sevm = new SEVM();
        Address origin = Address.of("0x1234567890abcdef");
        Address contractAddress = Address.of("0x0101010101010102");
        byte[] bytecode = new byte[0];

        EVMResult result = sevm.executeTransaction(origin, contractAddress, bytecode, 0, 1000000);
        System.out.println("EVM Result: " + result);
        assertTrue(result.isSuccess());
        assertEquals(999976, result.getGasRemaining());
        assertEquals(NumUtils.bytesToHex(new byte[]{0, 0, 0, 0, 0, 0, 0, 58}), NumUtils.bytesToHex(result.getReturnData()));
    }

    // 簡單的合約初始化代碼（返回 "Hello World" 的字節碼）
    private static final byte[] SIMPLE_INIT_CODE = new byte[]{
            Opcode.PUSH8.getCode(), 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f,  // PUSH8
            Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (memory offset)
            Opcode.MSTORE.getCode(),        // MSTORE
            Opcode.PUSH3.getCode(), 0x72, 0x6c, 0x64,  // PUSH3
            Opcode.PUSH1.getCode(), 0x08,  // PUSH1 8 (memory offset)
            Opcode.MSTORE.getCode(),        // MSTORE
            Opcode.PUSH1.getCode(), 0x10,  // PUSH1 16 (return size)
            Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0 (return offset)
            Opcode.RETURN.getCode(),         // RETURN
            Opcode.STOP.getCode()          // STOP
    };

    // 預期的部署代碼（runtime code）
    private static final byte[] EXPECTED_DEPLOYED_CODE = {
            0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64
    };

    @Test
    @DisplayName("成功創建合約並轉入 0.1 ETH")
    public void testCreateContract() {
        SEVM sevm = new SEVM();
        Address caller = Address.of("0x1234567890abcdef");
        Address contractAddress = Address.of("0x1234567890123456");

        EVMResult result = sevm.create(caller, contractAddress, SIMPLE_INIT_CODE, 1000000L, 1000000L);
        System.out.println("EVM Result: " + result);
        assertTrue(result.isSuccess());

    }

}
