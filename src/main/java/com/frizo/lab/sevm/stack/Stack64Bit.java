package com.frizo.lab.sevm.stack;

import com.frizo.lab.sevm.exception.EVMException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Slf4j
public class Stack64Bit implements Stack<Long> {

    private final int STACK_LIMIT;
    private final Deque<Long> stack = new ArrayDeque<>();

    public Stack64Bit(int stackLimit) {
        this.STACK_LIMIT = stackLimit;
    }

    @Override
    public Long safePop() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack underflow");
        }
        return stack.pop();
    }

    @Override
    public List<Long> safePop(int count) {
        if (stack.size() < count) {
            log.error("[Stack64Bit] Not enough elements in stack to pop {} items. Current size: {}", count, stack.size());
            throw new EVMException.StackUnderflowException("Not enough elements in stack to pop " + count + " items. Current size: " + stack.size());
        }
        List<Long> poppedValues = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            poppedValues.add(stack.pop());
        }
        return poppedValues;
    }

    @Override
    public void safePush(Long value) {
        if (stack.size() >= STACK_LIMIT) {
            throw new RuntimeException("Stack overflow");
        }
        stack.push(value);
    }

    @Override
    public void printStack() {
        System.out.println("Stack: " + stack);
    }

    @Override
    public Long peek() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return stack.peek();
    }

    @Override
    public int size() {
        return stack.size();
    }

    @Override
    public Long get(int index) {
        if (index < 0 || index >= stack.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }

        int i = 0;
        for (Long val : stack) {
            if (i == index) {
                return val;
            }
            i++;
        }

        throw new IllegalStateException("Unexpected error in stack.get()");
    }

    @Override
    public void swap(int indexOfA, int indexOfB) {
        if (indexOfA < 0 || indexOfA >= stack.size() || indexOfB < 0 || indexOfB >= stack.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + indexOfA + " or " + indexOfB);
        }

        List<Long> temp = new ArrayList<>(stack);
        long A = temp.get(indexOfA);
        long B = temp.get(indexOfB);

        // 交換 top 與第 depth 個（注意先清空原 stack）
        temp.set(indexOfA, B);
        temp.set(indexOfB, A);

        stack.clear();
        for (int j = temp.size() - 1; j >= 0; j--) {
            stack.push(temp.get(j));
        }
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public void clear() {
        stack.clear();
    }
}
