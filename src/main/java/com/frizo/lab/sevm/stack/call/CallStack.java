package com.frizo.lab.sevm.stack.call;

import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.stack.Stack;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Slf4j
public class CallStack implements Stack<CallFrame> {

    private final Deque<CallFrame> frames;
    private final int maxDepth;

    public CallStack(int maxDepth) {
        this.frames = new ArrayDeque<>();
        this.maxDepth = maxDepth;
    }


    @Override
    public CallFrame safePop() {
        if (frames.isEmpty()) {
            throw new EVMException.CallStackUnderFlowException();
        }
        CallFrame popped = frames.pop();
        log.info("[CallStack] Popped frame, depth: {}", frames.size());

        CallFrame previousFrame = frames.peek();

        if (previousFrame!= null && popped.isSuccess()) {
            // Set return data to previous frame buffer.
            previousFrame.cacheReturn(popped.getReturnOffset(), popped.getReturnSize(), popped.getReturnData());
            // Collect logs to previous frame buffer.
            List<LogEntry> logs = popped.getLogs();
            previousFrame.addLogs(logs);
            log.info("[CallStack] Collected {} logs to previous frame:{}", logs.size(), previousFrame.getFrameId());
        }

        if (previousFrame!= null && popped.isReverted()) {
            // Set revert reason to previous frame buffer.
            previousFrame.cacheReverted(popped.getRevertReason());
        }

        return popped;
    }

    @Override
    public List<CallFrame> safePop(int count) {
        throw new UnsupportedOperationException("safePop with count is not supported in CallStack");
    }

    @Override
    public void safePush(CallFrame frame) {
        if (frames.size() >= maxDepth) {
            throw new EVMException.CallStackOverFlowException();
        }
        frames.push(frame);
        log.info("[CallStack] Pushed frame, depth: {}", frames.size());
    }

    @Override
    public void printStack() {
        log.info("[CallStack] Current stack depth: {}", frames.size());
        for (CallFrame frame : frames) {
            log.info("[CallStack] Frame: {}", frame);
        }
    }

    @Override
    public CallFrame peek() {
        if (frames.isEmpty()) {
            throw new EVMException.CallStackUnderFlowException();
        }
        return frames.peek();
    }

    @Override
    public int size() {
        return frames.size();
    }

    @Override
    public CallFrame get(int index) {
        if (frames.isEmpty()) {
            throw new EVMException.CallStackUnderFlowException();
        }
        return frames.pop();
    }

    @Override
    public void swap(int indexOfA, int indexOfB) {
        if (indexOfA < 0 || indexOfA >= frames.size() || indexOfB < 0 || indexOfB >= frames.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + indexOfA + " or " + indexOfB);
        }

        List<CallFrame> temp = new ArrayList<>(frames);
        CallFrame A = temp.get(indexOfA);
        CallFrame B = temp.get(indexOfB);

        // 交換 top 與第 depth 個（注意先清空原 stack）
        temp.set(indexOfA, B);
        temp.set(indexOfB, A);

        frames.clear();
        for (int j = temp.size() - 1; j >= 0; j--) {
            frames.push(temp.get(j));
        }
    }

    @Override
    public boolean isEmpty() {
        return frames.isEmpty();
    }

    @Override
    public void clear() {
        frames.clear();
    }
}
