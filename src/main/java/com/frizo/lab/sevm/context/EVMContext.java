package com.frizo.lab.sevm.context;

import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.memory.Memory;
import com.frizo.lab.sevm.nums.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.storage.Storage;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class EVMContext {

    private final Stack<Integer> stack;
    private final Memory<Integer, byte[]> memory;
    private final Storage<Integer, byte[]> storage;

    private final byte[] code;
    private int pc;
    private int gasRemaining;
    private int gasUsed;
    private boolean running;
    private final Set<Integer> validJumpDestIdx;

    public EVMContext(byte[] bytecode, int initialGas) {
        this.stack = EVMComponentFactory.createStack(1024);
        this.memory = EVMComponentFactory.createMemory();
        this.storage = EVMComponentFactory.createStorage();
        this.code = bytecode;
        this.pc = 0;
        this.gasRemaining = initialGas;
        this.running = true;
        this.validJumpDestIdx = new HashSet<>();
    }

    // 統一的狀態操作方法
    public void consumeGas(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Gas amount must be non-negative");
        }
        if (gasRemaining < amount) {
            throw new EVMException.OutOfGasException();
        }
        gasRemaining -= amount;
        gasUsed += amount;
    }

    public void stop() {
        running = false;
    }

    public void updatePC(int idx) {
        pc = idx;
    }

    public boolean hasMoreCode() {
        return pc < code.length;
    }

    public Opcode getCurrentOpcode() {
        if (!hasMoreCode()) {
            throw new EVMException.NoMoreCodeException();
        }
        return Opcode.fromByte(code[pc]);
    }

    public void advancePC() {
        pc++;
    }

    public void halt() {
        running = false;
    }
}
