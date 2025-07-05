package com.frizo.lab.sevm.memory;

public interface Memory<K, V> {

    V get(K key);
    void put(K key, V value);
    boolean containsKey(K key);
    void clear(K key);
    void printMemory();
}
