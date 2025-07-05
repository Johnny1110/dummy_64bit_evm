package com.frizo.lab.stack;

public interface Stack<T> {
    
    T safePop();
    void safePush(T value);
    void printStack();

    T peek();
}
