package com.frizo.lab.sevm.memory;

import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * This DummyMemory class simulates a memory storage for the Ethereum Virtual Machine (EVM).
 * It uses a HashMap to store key-value pairs where keys are integers (representing memory
 * addresses) and values are byte arrays (representing the data stored at those addresses).
 * It allows for basic operations like getting, putting, checking existence of keys,
 * clearing keys, and printing the memory contents.
 * <p>
 * In a real EVM implementation, memory is a temporary storage that is used during the execution of smart contracts.
 * It is not persistent and is cleared after the execution of a transaction.
 * This DummyMemory class is a simplified version and does not implement all EVM memory features.
 */
@Slf4j
public class DummyMemory implements Memory<Integer, byte[]> {

    private final Map<Integer, byte[]> M = new HashMap<>();

    @Override
    public byte get(Integer key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void put(Integer key, byte value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsKey(Integer key) {
        return M.containsKey(key);
    }

    @Override
    public void clear(Integer key) {
        if (M.containsKey(key)) {
            M.remove(key);
        } else {
            throw new NullPointerException("invalid address: " + key);
        }
    }

    @Override
    public void clear(Integer offset, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printMemory() {
        System.out.println("Memory contents:");
        for (Map.Entry<Integer, byte[]> entry : M.entrySet()) {
            System.out.printf("Address: %d, Value: %s%n", entry.getKey(), NumUtils.bytesToHex(entry.getValue()));
        }
    }

    @Override
    public void cleanUp() {
        M.clear();
        log.info("DummyMemory cleaned up.");
    }

    @Override
    public void put(long offset, long maxLength, byte[] value) {
        throw new UnsupportedOperationException("Deprecated Class: DummyMemory does not support put with offset and maxLength");
    }

    @Override
    public byte[] get(long offset, long length) {
        throw new UnsupportedOperationException("Deprecated Class: DummyMemory does not support get with offset and length");
    }
}
