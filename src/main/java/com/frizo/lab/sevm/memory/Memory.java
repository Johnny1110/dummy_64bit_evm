package com.frizo.lab.sevm.memory;

public interface Memory<K, V> {

    @Deprecated(forRemoval = true)
    V get(K key);

    @Deprecated(forRemoval = true)
    void put(K key, V value);

    boolean containsKey(K offset);

    void clear(K offset);

    void clear(K offset, long length);

    void printMemory();

    void cleanUp();

    void put(long offset, long maxLength, V value);

    V get(long offset, long length);
}
