package com.frizo.lab.sevm.stack;

import java.util.List;

public interface Stack<T> {

    T safePop();

    List<T> safePop(int count);

    void safePush(T value);

    void printStack();

    T peek();

    int size();

    T get(int index);

    void swap(int indexA, int indexB);

    boolean isEmpty();

    void clear();
}
