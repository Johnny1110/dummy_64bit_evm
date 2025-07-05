package com.frizo.lab;

import com.frizo.lab.nums.Opcode;
import com.frizo.lab.utils.NumUtils;

import java.util.*;
import java.util.function.BinaryOperator;

public class SimpleEVM {
    private Deque<Integer> stack = new ArrayDeque<Integer>();
    private byte[] code;
    private int pc = 0; // program counter
    private boolean running = true;

    private int gasRemaining;

    private final Map<Integer, byte[]> memory = new HashMap<>(); // mock EVM memory
    private final Map<Integer, byte[]> storage = new HashMap<>(); // mock EVM storage

    private final Set<Integer> validJumpDestIdx = new HashSet<>();

    public SimpleEVM(byte[] bytecode, int initialGas) {
        this.code = bytecode;
        scanJumpDestinations();
        this.gasRemaining = initialGas;
    }

    public int getGasRemaining() {
        return gasRemaining;
    }

    /**
     * scan the bytecode to find valid jump destinations
     */
    private void scanJumpDestinations() {
        for (int i = 0; i < code.length; i++) {
            if (code[i] == Opcode.JUMPDEST.getCode()) {
                validJumpDestIdx.add(i);
            }
        }
    }

    public void run() {
        while (running && pc < code.length) {
            Opcode op = Opcode.fromByte(code[pc++]);

            consumeGas(op.getGasCost());

            if (op.isPush()) {

            }

            switch (op) {
                case STOP -> running = false;
                case ADD -> binaryOp((a, b) -> a + b);
                case MUL -> binaryOp((a, b) -> a * b);
                case SUB -> binaryOp((a, b) -> a - b);
                case DIV -> binaryOp((a, b) -> a / b);
                case PUSH1 -> {
                    byte value = code[pc++]; // get next input byte as value
                    stack.push((int) value); // push value onto the stack
                }
                case MSTORE -> {
                    int offset = stack.pop();
                    int value = stack.pop();
                    memory.put(offset, NumUtils.intTo32Bytes(value));
                }
                case MLOAD -> {
                    int offset = stack.pop();
                    byte[] data = memory.getOrDefault(offset, new byte[32]); // read data from memory
                    int value = NumUtils.bytes32ToInt(data);
                    stack.push(value);
                }

                case JUMPDEST -> {
                    // JUMPDEST does not affect stack or pc, just marks a valid jump destination
                    // No action needed here, just for validation
                }

                case JUMP -> {
                    int destIdx = stack.pop();
                    requiredValidJump(destIdx);
                    pc = destIdx; // set pc to the destination index
                }
                case JUMPI -> {
                    int dest = stack.pop();
                    int condition = stack.pop();
                    if (condition != 0) {
                        requiredValidJump(dest);
                        pc = dest; // set pc to the destination index if condition is true
                    }
                }

                case SSTORE -> {
                    int key = stack.pop();
                    int value = stack.pop();
                    storage.put(key, NumUtils.intTo32Bytes(value)); // store value in storage
                }

                case SLOAD -> {
                    int key = stack.pop();
                    int value = NumUtils.bytes32ToInt(storage.getOrDefault(key, new byte[32])); // load value from storage
                    stack.push(value);
                }

                default -> throw new RuntimeException("Unknown opcode " + op);
            }
        }
    }

    private void consumeGas(int gasCost) {
        if (gasRemaining < gasCost) {
            throw new RuntimeException("Out of gas");
        }
        gasRemaining -= gasCost;
    }

    private void requiredValidJump(int destIdx) {
        if (!validJumpDestIdx.contains(destIdx)) {
            throw new RuntimeException(String.format("Invalid jump destination %d", destIdx));
        }
    }

    private void binaryOp(BinaryOperator<Integer> op) {
        int b = stack.pop();
        int a = stack.pop();
        stack.push(op.apply(a, b));
    }

    public int peek() {
        return stack.peek();
    }

    public void printStack() {
        System.out.println("Stack: " + stack);
    }

    public void printMemory() {
        System.out.println("Memory:");
        for (var entry : memory.entrySet()) {
            System.out.printf("  [%d] = %s\n", entry.getKey(), bytesToHex(entry.getValue()));
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void printStorage() {
        System.out.println("Storage: ");
        for (var entry : storage.entrySet()) {
            System.out.printf("  [%d] = %s\n", entry.getKey(), bytesToHex(entry.getValue()));
        }
    }
}
