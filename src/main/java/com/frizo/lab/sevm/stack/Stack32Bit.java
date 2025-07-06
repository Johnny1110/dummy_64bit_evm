package com.frizo.lab.sevm.stack;

import com.frizo.lab.sevm.exception.EVMException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Slf4j
public class Stack32Bit implements Stack<Integer> {

    private final int STACK_LIMIT;
    private final Deque<Integer> stack = new ArrayDeque<>();

    public Stack32Bit(int stackLimit) {
        this.STACK_LIMIT = stackLimit;
    }

    @Override
    public Integer safePop() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack underflow");
        }
        return stack.pop();
    }

    @Override
    public List<Integer> safePop(int count) {
        if (stack.size() < count) {
            log.error("[Stack32Bit] Not enough elements in stack to pop {} items. Current size: {}", count, stack.size());
            throw new EVMException.StackUnderflowException("Not enough elements in stack to pop " + count + " items. Current size: " + stack.size());
        }
        List<Integer> poppedValues = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            poppedValues.add(stack.pop());
        }
        return poppedValues;
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

    @Override
    public int size() {
        return stack.size();
    }

    @Override
    public Integer get(int index) {
        if (index < 0 || index >= stack.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }

        int i = 0;
        for (Integer val : stack) {
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

        List<Integer> temp = new ArrayList<>(stack);
        int A = temp.get(indexOfA);
        int B = temp.get(indexOfB);

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
