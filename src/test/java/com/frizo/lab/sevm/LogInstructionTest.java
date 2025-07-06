package com.frizo.lab.sevm;

import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.utils.NumUtils;
import com.frizo.lab.sevm.vm.SimpleEVM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogInstructionTest {

    private static final String TEST_ORIGIN = "0x12345678";

    @Test
    @DisplayName("測試 LOG0：單一資料無 topic")
    public void testLog0WithoutTopics() {
        byte[] bytecode = {
                Opcode.PUSH1.getCode(), 0x45,  // PUSH1 69 ('E')
                Opcode.PUSH1.getCode(), 0x00,  // PUSH1 0
                Opcode.MSTORE.getCode(),       // MSTORE8
                Opcode.PUSH1.getCode(), 0x72,  // PUSH1 114 ('r')
                Opcode.PUSH1.getCode(), 0x01,  // PUSH1 1
                Opcode.MSTORE.getCode(),       // MSTORE8
                Opcode.PUSH1.getCode(), 0x72,  // PUSH1 114 ('r')
                Opcode.PUSH1.getCode(), 0x02,  // PUSH1 2
                Opcode.MSTORE.getCode(),       // MSTORE8
                Opcode.PUSH1.getCode(), 0x6F,  // PUSH1 111 ('o')
                Opcode.PUSH1.getCode(), 0x03,  // PUSH1 3
                Opcode.MSTORE.getCode(),       // MSTORE8
                Opcode.PUSH1.getCode(), 0x72,  // PUSH1 114 ('r')
                Opcode.PUSH1.getCode(), 0x04,  // PUSH1 4
                Opcode.MSTORE.getCode(),       // MSTORE8
                Opcode.PUSH1.getCode(), 0x05,       // size = 5
                Opcode.PUSH1.getCode(), 0x00,       // offset = 0
                Opcode.LOG0.getCode(),              // LOG0
                Opcode.STOP.getCode()               // STOP
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.run();

        List<LogEntry> logs = evm.getContext().getCurrentFrame().getLogs();
        assertEquals(1, logs.size());
        LogEntry log = logs.get(0);
        assertEquals(0, log.getTopics().size());
        System.out.println("LOG:" + logs.get(0));
        assertArrayEquals(NumUtils.stringToBytes("Error"), log.getData());
    }

    @Test
    @DisplayName("測試 LOG1：含一個 topic")
    public void testLog1WithOneTopic() {
        byte[] bytecode = {
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, (byte) 0xAB, // topic 0xAB 171
                Opcode.PUSH1.getCode(), 0x05,       // size
                Opcode.PUSH1.getCode(), 0x00,       // offset
                Opcode.LOG1.getCode(),
                Opcode.STOP.getCode()
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 1000, TEST_ORIGIN);
        evm.getContext().getMemory().put(0, NumUtils.stringToBytes("h"));
        evm.getContext().getMemory().put(1, NumUtils.stringToBytes("e"));
        evm.getContext().getMemory().put(2, NumUtils.stringToBytes("l"));
        evm.getContext().getMemory().put(3, NumUtils.stringToBytes("l"));
        evm.getContext().getMemory().put(4, NumUtils.stringToBytes("o"));
        evm.run();

        List<LogEntry> logs = evm.getContext().getCurrentFrame().getLogs();
        assertEquals(1, logs.size());
        LogEntry log = logs.get(0);
        assertEquals(1, log.getTopics().size());
        System.out.println("LOG:" + logs.get(0));
        assertArrayEquals(NumUtils.stringToBytes("hello"), log.getData());
        assertEquals("000000AB", String.format("%08X", log.getTopics().get(0)));  // 32-bit int
    }

    @Test
    @DisplayName("測試 LOG4：最多四個 topics")
    public void testLog4WithFourTopics() {
        byte[] bytecode = {
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x01,
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x02,
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x03,
                Opcode.PUSH4.getCode(), 0x00, 0x00, 0x00, 0x04,
                Opcode.PUSH1.getCode(), 0x03,       // size 3
                Opcode.PUSH1.getCode(), 0x00,       // offset 0
                Opcode.LOG4.getCode(),
                Opcode.STOP.getCode()
        };

        SimpleEVM evm = new SimpleEVM(bytecode, 10000000, TEST_ORIGIN);
        evm.getContext().getMemory().put(0, NumUtils.stringToBytes("a"));
        evm.getContext().getMemory().put(1, NumUtils.stringToBytes("b"));
        evm.getContext().getMemory().put(2, NumUtils.stringToBytes("c"));
        evm.run();

        List<LogEntry> logs = evm.getContext().getCurrentFrame().getLogs();
        assertEquals(1, logs.size());
        LogEntry log = logs.get(0);
        assertEquals(4, log.getTopics().size());
        assertEquals(0x04, log.getTopics().get(0).intValue());
        assertEquals(0x01, log.getTopics().get(3).intValue());
        System.out.println("LOG:" + logs.get(0));
        assertArrayEquals(NumUtils.stringToBytes("abc"), log.getData());
    }

}
