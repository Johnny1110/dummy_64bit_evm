package com.frizo.lab.sevm.memory;

import com.frizo.lab.sevm.common.Constant;
import com.frizo.lab.sevm.utils.NumUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DummyMemory implements Memory<Integer, byte[]> {

    private static final Map<Integer, byte[]> M = new HashMap<>();

    @Override
    public byte[] get(Integer key) {
        if (!M.containsKey(key)) {
            throw new NullPointerException("invalid address: " + key);
        }
        byte[] value = M.get(key);
        // return empty byte array if no value is found
        return Objects.requireNonNullElseGet(value, () -> new byte[Constant.MAX_BYTES]);
    }

    @Override
    public void put(Integer key, byte[] value) {
        if (value == null) {
            M.put(key, new byte[Constant.MAX_BYTES]);
        }
        else if (value.length > Constant.MAX_BYTES) {
            throw new IllegalArgumentException("Value exceeds maximum size of " + Constant.MAX_BYTES + " bytes");
        } else {
            M.put(key, value);
        }
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
    public void printMemory() {
        System.out.println("Memory contents:");
        for (Map.Entry<Integer, byte[]> entry : M.entrySet()) {
            System.out.printf("Address: %d, Value: %s%n", entry.getKey(), NumUtils.bytesToHex(entry.getValue()));
        }
    }
}
