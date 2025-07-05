package com.frizo.lab.sevm.storage;

import com.frizo.lab.sevm.common.Constant;
import com.frizo.lab.sevm.utils.NumUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DummyStorage implements Storage<Integer, byte[]> {

    private static final Map<Integer, byte[]> S = new HashMap<>();

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
