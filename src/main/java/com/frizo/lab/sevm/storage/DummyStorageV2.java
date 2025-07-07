package com.frizo.lab.sevm.storage;

import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class DummyStorageV2 implements Storage<Long, Long> {

    private final Map<Long, Byte> S = new HashMap<>();

    @Override
    public byte get(Long key) {
        if (!S.containsKey(key)) {
            log.error("Attempted to get storage at invalid address: {}", key);
            throw new NullPointerException("Invalid address: " + key);
        }
        return S.getOrDefault(key, (byte) 0x00);
    }

    @Override
    public void put(Long key, byte value) {
        if (key < 0) {
            log.error("Attempted to put storage at negative offset: {}", key);
            throw new IllegalArgumentException("Offset must be non-negative");
        }
        S.put(key, value);
        log.info("Put value {} at offset {}", NumUtils.byteToHex(value), key);
    }

    @Override
    public boolean containsKey(Long offset) {
        if (offset < 0 || offset >= S.size()) {
            throw new IndexOutOfBoundsException("Offset out of bounds: " + offset);
        }
        return S.containsKey(offset);
    }

    @Override
    public void clear(Long key) {
        if (S.containsKey(key)) {
            S.remove(key);
        } else {
            throw new NullPointerException("Invalid address: " + key);
        }
    }

    @Override
    public void clear(Long offset, long length) {
        if (length <= 0) {
            log.error("Attempted to clear storage with non-positive length: {}", length);
            throw new IllegalArgumentException("Length must be positive");
        }

        if (offset < 0) {
            log.error("Attempted to clear storage with negative offset: {}", offset);
            throw new IllegalArgumentException("Offset must be non-negative");
        }

        if (offset + length < offset) {
            log.error("Overflow detected when calculating end offset: {} + {}", offset, length);
            throw new RuntimeException("Overflow detected");
        }

        if (offset + length > S.size()) {
            log.error("Attempted to clear storage beyond its size: offset {} + length {} exceeds storage size {}",
                    offset, length, S.size());
            throw new RuntimeException("Attempted to clear storage beyond its size");
        }

        for (long i = offset; i < offset + length; i++) {
            S.remove(i);
        }
        log.info("Cleared DummyStorageV2 from offset {} to {}", offset, offset + length);
    }

    @Override
    public void printStorage() {
        System.out.println("-- DummyStorageV2 contents ------------------------------------------------------>");
        System.out.println("Storage contents:");
        for (Map.Entry<Long, Byte> entry : S.entrySet()) {
            System.out.printf("Address: %d, Value: %s%n", entry.getKey(), NumUtils.byteToHex(entry.getValue()));
        }
        System.out.println("<------------------------------------------------------ DummyStorageV2 contents --");
    }

    @Override
    public void put(long offset, long maxLength, Long value) {
        if (maxLength <= 0) {
            log.error("Attempted to put storage with non-positive maxLength: {}", maxLength);
            throw new IllegalArgumentException("maxLength must be positive");
        }
        if (maxLength > 8) {
            log.error("Attempted to put storage with maxLength greater than 8 bytes: {}", maxLength);
            throw new IllegalArgumentException("maxLength must not exceed 8 bytes");
        }

        if (offset < 0) {
            log.error("Attempted to put storage with negative offset: {}", offset);
            throw new IllegalArgumentException("Offset must be non-negative");
        }

        if (offset + maxLength < offset) {
            log.error("Overflow detected when calculating end offset: {} + {}", offset, maxLength);
            throw new RuntimeException("Overflow detected");
        }

        byte[] bytes = NumUtils.longToBytesWithPadding(value, (int) maxLength);
        for (int i = 0; i < bytes.length; i++) {
            long currentOffset = offset + i;
            if (currentOffset < 0) {
                log.error("Attempted to put storage at negative offset: {}", currentOffset);
                throw new IllegalArgumentException("Offset must be non-negative");
            }
            S.put(currentOffset, bytes[i]);
        }
    }

    @Override
    public Long get(long offset, long length) {
        if (length <= 0) {
            log.error("Attempted to get storage with non-positive length: {}", length);
            throw new IllegalArgumentException("Length must be positive");
        }

        if (length > 8) {
            log.error("Attempted to get storage with length greater than 8 bytes: {}", length);
            throw new IllegalArgumentException("Length must not exceed 8 bytes");
        }

        if (offset < 0) {
            log.error("Attempted to get storage with negative offset: {}", offset);
            throw new IllegalArgumentException("Offset must be non-negative");
        }

        if (offset + length < offset) {
            log.error("Overflow detected when calculating end offset: {} + {}", offset, length);
            throw new RuntimeException("Overflow detected");
        }

        byte[] bytes = new byte[(int) length];
        for (long i = 0; i < length; i++) {
            long currentOffset = offset + i;
            bytes[(int) i] = S.getOrDefault(currentOffset, (byte) 0);
        }

        return NumUtils.paddingBytesToLong(bytes, (int) length);
    }

}