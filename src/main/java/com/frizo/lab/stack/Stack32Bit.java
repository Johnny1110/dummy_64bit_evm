package com.frizo.lab.stack;

import java.util.ArrayDeque;
import java.util.Deque;

public class Stack32Bit implements Stack<Integer> {

    private static final int STACK_LIMIT = 1024;
    private final Deque<Integer> stack = new ArrayDeque<>();

    @Override
    public Integer safePop() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack underflow");
        }
        return stack.pop();
    }

    @Override
    public void safePush(Integer value) {
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
    public Integer peek() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return stack.peek();
    }
}
