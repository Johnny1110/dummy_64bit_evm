package com.frizo.lab.sevm.context;

import com.frizo.lab.sevm.memory.DummyMemory;
import com.frizo.lab.sevm.memory.Memory;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.stack.Stack32Bit;
import com.frizo.lab.sevm.storage.DummyStorage;
import com.frizo.lab.sevm.storage.Storage;

public class EVMComponentFactory {

    public static Stack<Integer> createStack(int limit) {
        return new Stack32Bit(limit);
    }

    public static Memory<Integer, byte[]> createMemory() {
        return new DummyMemory();
    }

    public static Storage<Integer, byte[]> createStorage() {
        return new DummyStorage();
    }

}
