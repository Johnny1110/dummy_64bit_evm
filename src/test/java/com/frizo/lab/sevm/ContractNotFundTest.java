package com.frizo.lab.sevm;

import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.vm.SimpleEVM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.frizo.lab.sevm.TestConstant.TEST_ORIGIN;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContractNotFundTest {

    @Test
    @DisplayName("測試找不到合約時的行為")
    public void testCallContextPreservation() {
        // test bytecode to check if call context is preserved after a call
        byte[] bytecode = {
                // put some test data
                Opcode.PUSH1.getCode(), 0x11,  // data 1
                Opcode.PUSH1.getCode(), 0x22,  // data 2
                Opcode.PUSH1.getCode(), 0x33,  // data 3

                // call func
                Opcode.PUSH1.getCode(), 0x00,  // retSize
                Opcode.PUSH1.getCode(), 0x00,  // retOffset
                Opcode.PUSH1.getCode(), 0x00,  // argsSize
                Opcode.PUSH1.getCode(), 0x00,  // argsOffset
                Opcode.PUSH1.getCode(), 0x00,  // value
                Opcode.PUSH4.getCode(), 0x01, (byte) 0xC1, (byte) 0xA1, (byte) 0x00,  // address
                Opcode.PUSH1.getCode(), 0x64,  // gas
                Opcode.CALL.getCode(),         // CALL

                Opcode.STOP.getCode()           // STOP
        };

        var evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        System.out.println("Call context preservation test completed");
        evm.printStack();

        assertEquals(4, evm.getContext().getCurrentStack().size());
        assertEquals(0x00, evm.getContext().getCurrentStack().safePop());
        assertEquals(0x33, evm.getContext().getCurrentStack().safePop());
        assertEquals(0x22, evm.getContext().getCurrentStack().safePop());
        assertEquals(0x11, evm.getContext().getCurrentStack().safePop());
    }

}
