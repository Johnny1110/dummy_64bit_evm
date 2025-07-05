package com.frizo.lab.sevm.storage;

import com.frizo.lab.sevm.common.Constant;
import com.frizo.lab.sevm.utils.NumUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * In real Ethereum Virtual Machine (EVM) implementations, the storage is a key-value store
 * where keys are 256-bit integers and values are 256-bit byte arrays.
 * This DummyStorage class simulates such a storage using a HashMap.
 * It allows for basic operations like getting, putting, checking existence of keys,
 * clearing keys, and printing the storage contents.
 * This is a simplified version and does not implement all EVM storage features.
 *
 * Real EVM storage would also handle gas costs, state changes, and more complex data structures.
 * Each Smart Contract would have its own storage, and the storage would be persistent across transactions.
 */
public class DummyStorage implements Storage<Integer, byte[]> {

    private final Map<Integer, byte[]> S = new HashMap<>();

    @Override
    public byte[] get(Integer key) {
        if (!S.containsKey(key)) {
            throw new NullPointerException("invalid address: " + key);
        }
        byte[] value = S.get(key);
        // return empty byte array if no value is found
        return Objects.requireNonNullElseGet(value, () -> new byte[Constant.MAX_BYTES]);
    }

    @Override
    public void put(Integer key, byte[] value) {
        if (value == null) {
            S.put(key, new byte[Constant.MAX_BYTES]);
        }
        else if (value.length > Constant.MAX_BYTES) {
            throw new IllegalArgumentException("Value exceeds maximum size of " + Constant.MAX_BYTES + " bytes");
        } else {
            S.put(key, value);
        }
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
    public void printStorage() {
        System.out.println("Storage contents:");
        for (Map.Entry<Integer, byte[]> entry : S.entrySet()) {
            System.out.printf("Address: %d, Value: %s%n", entry.getKey(), NumUtils.bytesToHex(entry.getValue()));
        }
    }
}
