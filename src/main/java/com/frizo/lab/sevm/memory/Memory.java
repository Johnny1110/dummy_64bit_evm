package com.frizo.lab.sevm.memory;

public interface Memory<K, V> {

    byte get(K address);

    void put(K address, byte value);

    boolean containsKey(K offset);

    void clear(K offset);

    void clear(K offset, long length);

    void printMemory();

    void cleanUp();

    void put(long offset, long maxLength, V value);

    V get(long offset, long length);
}
