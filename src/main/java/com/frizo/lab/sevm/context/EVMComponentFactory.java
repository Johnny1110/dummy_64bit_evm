package com.frizo.lab.sevm.context;

import com.frizo.lab.sevm.memory.DummyMemoryV2;
import com.frizo.lab.sevm.memory.Memory;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.stack.Stack64Bit;
import com.frizo.lab.sevm.storage.DummyStorageV2;
import com.frizo.lab.sevm.storage.Storage;

public class EVMComponentFactory {

    public static Stack<Long> createStack(int limit) {
        return new Stack64Bit(limit);
    }

    public static Memory<Long, Long> createMemory() {
        return new DummyMemoryV2();
    }

    public static Storage<Long, Long> createStorage() {
        return new DummyStorageV2();
    }

}
