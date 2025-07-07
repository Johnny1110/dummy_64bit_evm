package com.frizo.lab.sevm.memory;

import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DummyMemoryV2 implements Memory<Long, Long> {

    private final Map<Long, Byte> memory = new HashMap<>();

    @Override
    public Long get(Long key) {
        throw new UnsupportedOperationException("Deprecated Class: DummyMemory does not support get with offset and length");
    }

    @Override
    public void put(Long key, Long value) {
        throw new UnsupportedOperationException("Deprecated Class: DummyMemory does not support put with offset and length");
    }

    @Override
    public boolean containsKey(Long offset) {
        return memory.containsKey(offset);
    }

    @Override
    public void clear(Long key) {
        memory.remove(key);
    }

    @Override
    public void clear(Long offset, long length) {
        if (length <= 0) {
            log.error("Attempted to clear memory with non-positive length: {}", length);
            throw new IllegalArgumentException("Length must be positive");
        }

        if (offset < 0) {
            log.error("Attempted to clear memory with negative offset: {}", offset);
            throw new IllegalArgumentException("Offset must be non-negative");
        }

        if (offset + length < offset) {
            log.error("Overflow detected when calculating end offset: {} + {}", offset, length);
            throw  new RuntimeException("Overflow detected");
        }

        if (offset + length > memory.size()) {
            log.error("Attempted to clear memory beyond its size: offset {} + length {} exceeds memory size {}",
                      offset, length, memory.size());
            throw new RuntimeException("Attempted to clear memory beyond its size");
        }

        for (long i = offset; i < offset + length; i++) {
            memory.remove(i);
        }
        log.info("Cleared DummyMemoryV2 from offset {} to {}", offset, offset + length);
    }

    @Override
    public void printMemory() {
        System.out.println("-- DummyMemoryV2 contents ------------------------------------------------------>");
        for (Map.Entry<Long, Byte> entry : memory.entrySet()) {
            System.out.printf("Address: %d, Value: %d%n", entry.getKey(), NumUtils.byteToHex(entry.getValue()));
        }
        System.out.println("-- DummyMemoryV2 contents ------------------------------------------------------>");
    }

    @Override
    public void cleanUp() {
        memory.clear();
    }

    @Override
    public void put(long offset, long maxLength, Long value) {
        if (maxLength <= 0) {
            log.error("Attempted to put memory with non-positive maxLength: {}", maxLength);
            throw new IllegalArgumentException("maxLength must be positive");
        }
        if (maxLength > 8) {
            log.error("Attempted to put memory with maxLength greater than 8 bytes: {}", maxLength);
            throw new IllegalArgumentException("maxLength must not exceed 8 bytes");
        }

        if (offset < 0) {
            log.error("Attempted to put memory with negative offset: {}", offset);
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
                log.error("Attempted to put memory at negative offset: {}", currentOffset);
                throw new IllegalArgumentException("Offset must be non-negative");
            }
            memory.put(currentOffset, bytes[i]);
        }
    }

    @Override
    public Long get(long offset, long length) {
        if (length <= 0) {
            log.error("Attempted to get memory with non-positive length: {}", length);
            throw new IllegalArgumentException("Length must be positive");
        }

        if (length > 8) {
            log.error("Attempted to get memory with length greater than 8 bytes: {}", length);
            throw new IllegalArgumentException("Length must not exceed 8 bytes");
        }

        if (offset < 0) {
            log.error("Attempted to get memory with negative offset: {}", offset);
            throw new IllegalArgumentException("Offset must be non-negative");
        }

        if (offset + length < offset) {
            log.error("Overflow detected when calculating end offset: {} + {}", offset, length);
            throw new RuntimeException("Overflow detected");
        }

        if (offset + length > memory.size()) {
            log.error("Attempted to get memory beyond its size: offset {} + length {} exceeds memory size {}",
                      offset, length, memory.size());
            throw new RuntimeException("Attempted to get memory beyond its size");
        }

        byte[] bytes = new byte[(int) length];
        for (long i = 0; i < length; i++) {
            long currentOffset = offset + i;
            bytes[(int) i] = memory.getOrDefault(currentOffset, (byte) 0);
        }

        return NumUtils.paddingBytesToLong(bytes, (int) length);
    }
}
