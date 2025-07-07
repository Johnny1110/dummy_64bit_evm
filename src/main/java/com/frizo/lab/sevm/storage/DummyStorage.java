package com.frizo.lab.sevm.storage;

import com.frizo.lab.sevm.utils.NumUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * In real Ethereum Virtual Machine (EVM) implementations, the storage is a key-value store
 * where keys are 256-bit integers and values are 256-bit byte arrays.
 * This DummyStorage class simulates such a storage using a HashMap.
 * It allows for basic operations like getting, putting, checking existence of keys,
 * clearing keys, and printing the storage contents.
 * This is a simplified version and does not implement all EVM storage features.
 * <p>
 * Real EVM storage would also handle gas costs, state changes, and more complex data structures.
 * Each Smart Contract would have its own storage, and the storage would be persistent across transactions.
 */
public class DummyStorage implements Storage<Integer, byte[]> {

    private final Map<Integer, byte[]> S = new HashMap<>();


    @Override
    public byte get(Integer key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(Integer key, byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Integer key) {
        return S.containsKey(key);
    }

    @Override
    public void clear(Integer key) {
        if (S.containsKey(key)) {
            S.remove(key);
        } else {
            throw new NullPointerException("invalid address: " + key);
        }
    }

    @Override
    public void clear(Integer offset, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printStorage() {
        System.out.println("Storage contents:");
        for (Map.Entry<Integer, byte[]> entry : S.entrySet()) {
            System.out.printf("Address: %d, Value: %s%n", entry.getKey(), NumUtils.bytesToHex(entry.getValue()));
        }
    }

    @Override
    public void put(long offset, long maxLength, byte[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] get(long offset, long length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
