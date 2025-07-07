package com.frizo.lab.sevm.storage;

public interface Storage<K, V> {

    byte get(K key);

    void put(K key, byte value);

    boolean containsKey(K key);

    void clear(K key);

    void clear(K offset, long length);

    void printStorage();

    void put(long offset, long maxLength, V value);

    V get(long offset, long length);
}
