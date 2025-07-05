package com.frizo.lab.sevm.stack;

public interface Stack<T> {
    
    T safePop();
    void safePush(T value);
    void printStack();
    T peek();
    int size();
    T get(int index);
    void swap(int indexA, int indexB);
    boolean isEmpty();
    void clear();
}
