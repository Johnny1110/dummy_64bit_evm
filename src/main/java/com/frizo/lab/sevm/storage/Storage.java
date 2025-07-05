package com.frizo.lab.sevm.storage;

public interface Storage<K, V> {

    V get(K key);
    void put(K key, V value);
    boolean containsKey(K key);
    void clear(K key);
    void printStorage();
}
