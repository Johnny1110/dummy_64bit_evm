package com.frizo.lab;

import com.frizo.lab.nums.Opcode;
import com.frizo.lab.stack.Stack;
import com.frizo.lab.stack.Stack32Bit;
import com.frizo.lab.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.BinaryOperator;

@Slf4j
public class SimpleEVM {

    // Stack limit for the EVM, to prevent stack overflow
    private static final int STACK_LIMIT = 1024;


    private final Stack<Integer> stack;
    private final byte[] code;
    private int pc = 0; // program counter
    private boolean running = true;

    private int gasRemaining;

    private final Map<Integer, byte[]> memory = new HashMap<>(); // mock EVM memory
    private final Map<Integer, byte[]> storage = new HashMap<>(); // mock EVM storage

    private final Set<Integer> validJumpDestIdx = new HashSet<>();

    public SimpleEVM(byte[] bytecode, int initialGas) {
        this.stack = new Stack32Bit();
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
                int pushSize = op.getCode() - Opcode.PUSH1.getCode() + 1; // PUSH1 is 0x60, PUSH2 is 0x61, etc.
                log.info("Executing PUSH operation: {}, size: {}", op, pushSize);
                byte[] pushData = new byte[pushSize];
                System.arraycopy(code, pc, pushData, 0, pushSize);
                pc += pushSize; // move pc forward by the size of the push data
                int value = NumUtils.bytesToInt(pushData);
                stack.safePush(value);
                continue; // skip further processing for PUSH opcodes
            }

            switch (op) {
                case STOP -> running = false;
                case ADD -> binaryOp((a, b) -> a + b);
                case MUL -> binaryOp((a, b) -> a * b);
                case SUB -> binaryOp((a, b) -> a - b);
                case DIV -> binaryOp((a, b) -> a / b);

                case MSTORE -> {
                    int offset = stack.safePop();
                    int value = stack.safePop();
                    memory.put(offset, NumUtils.intTo4Bytes(value));
                }
                case MLOAD -> {
                    int offset = stack.safePop();
                    byte[] data = memory.getOrDefault(offset, new byte[4]); // read data from memory
                    int value = NumUtils.bytes4ToInt(data);
                    stack.safePush(value);
                }

                case JUMPDEST -> {
                    // JUMPDEST does not affect stack or pc, just marks a valid jump destination
                    // No action needed here, just for validation
                }

                case JUMP -> {
                    int destIdx = stack.safePop();
                    requiredValidJump(destIdx);
                    pc = destIdx; // set pc to the destination index
                }
                case JUMPI -> {
                    int dest = stack.safePop();
                    int condition = stack.safePop();
                    if (condition != 0) {
                        requiredValidJump(dest);
                        pc = dest; // set pc to the destination index if condition is true
                    }
                }

                case SSTORE -> {
                    int key = stack.safePop();
                    int value = stack.safePop();
                    storage.put(key, NumUtils.intTo4Bytes(value)); // store value in storage
                }

                case SLOAD -> {
                    int key = stack.safePop();
                    int value = NumUtils.bytes4ToInt(storage.getOrDefault(key, new byte[4])); // load value from storage
                    stack.safePush(value);
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
        int b = stack.safePop();
        int a = stack.safePop();
        stack.safePush(op.apply(a, b));
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
