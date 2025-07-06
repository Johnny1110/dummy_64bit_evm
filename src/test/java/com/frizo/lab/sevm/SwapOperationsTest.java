package com.frizo.lab.sevm;

import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.vm.SimpleEVM;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SWAP Operations Unit Tests")
public class SwapOperationsTest {

    private static final int INITIAL_GAS = 100000;

    // Helper method to get SWAP opcode by index
    private Opcode getSwapOpcode(int index) {
        switch (index) {
            case 1:
                return Opcode.SWAP1;
            case 2:
                return Opcode.SWAP2;
            case 3:
                return Opcode.SWAP3;
            case 4:
                return Opcode.SWAP4;
            case 5:
                return Opcode.SWAP5;
            case 6:
                return Opcode.SWAP6;
            case 7:
                return Opcode.SWAP7;
            case 8:
                return Opcode.SWAP8;
            case 9:
                return Opcode.SWAP9;
            case 10:
                return Opcode.SWAP10;
            case 11:
                return Opcode.SWAP11;
            case 12:
                return Opcode.SWAP12;
            case 13:
                return Opcode.SWAP13;
            case 14:
                return Opcode.SWAP14;
            case 15:
                return Opcode.SWAP15;
            case 16:
                return Opcode.SWAP16;
            default:
                throw new IllegalArgumentException("Invalid SWAP index: " + index);
        }
    }

    // Helper method to convert List<Byte> to byte[]
    private byte[] toByteArray(List<Byte> list) {
        byte[] array = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    @Nested
    @DisplayName("SWAP1 Tests")
    class Swap1Tests {

        @Test
        @DisplayName("SWAP1 should swap top two elements")
        void testSwap1Basic() {
            // Arrange: Create bytecode that pushes 10, 20, then swaps
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 10,    // PUSH1 10
                    Opcode.PUSH1.getCode(), 20,    // PUSH1 20
                    Opcode.SWAP1.getCode()         // SWAP1
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act: Execute the bytecode
            evm.run();

            // Assert: Stack should have [10, 20] (top to bottom)
            assertEquals(10, evm.peek());
            evm.getStack().safePop();
            assertEquals(20, evm.peek());
        }

        @Test
        @DisplayName("SWAP1 should maintain stack size")
        void testSwap1StackSize() {
            // Arrange
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), (byte) 100,   // PUSH1 100
                    Opcode.PUSH1.getCode(), (byte) 200,   // PUSH1 200
                    Opcode.SWAP1.getCode()         // SWAP1
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Stack should have exactly 2 elements
            assertEquals(2, evm.getStack().size());
        }

        @Test
        @DisplayName("SWAP1 with multiple values")
        void testSwap1WithMultipleValues() {
            // Arrange: Push 1, 2, 3, 4, then SWAP1
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 1,     // PUSH1 1
                    Opcode.PUSH1.getCode(), 2,     // PUSH1 2
                    Opcode.PUSH1.getCode(), 3,     // PUSH1 3
                    Opcode.PUSH1.getCode(), 4,     // PUSH1 4
                    Opcode.SWAP1.getCode()         // SWAP1
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Stack should be [3, 4, 2, 1] (top to bottom)
            Stack<Integer> stack = evm.getStack();
            assertEquals(3, stack.safePop());  // Top
            assertEquals(4, stack.safePop());  // Second
            assertEquals(2, stack.safePop());  // Third
            assertEquals(1, stack.safePop());  // Bottom
        }
    }

    @Nested
    @DisplayName("SWAP2 Tests")
    class Swap2Tests {

        @Test
        @DisplayName("SWAP2 should swap top and third elements")
        void testSwap2Basic() {
            // Arrange: Push 10, 20, 30, then SWAP2
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 10,    // PUSH1 10
                    Opcode.PUSH1.getCode(), 20,    // PUSH1 20
                    Opcode.PUSH1.getCode(), 30,    // PUSH1 30
                    Opcode.SWAP2.getCode()         // SWAP2
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Stack should be [10, 20, 30] (top to bottom)
            Stack<Integer> stack = evm.getStack();
            assertEquals(10, stack.safePop());  // Top (was bottom)
            assertEquals(20, stack.safePop());  // Middle (unchanged)
            assertEquals(30, stack.safePop());  // Bottom (was top)
        }

        @Test
        @DisplayName("SWAP2 with 4 elements")
        void testSwap2WithFourElements() {
            // Arrange: Push 1, 2, 3, 4, then SWAP2
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 1,     // PUSH1 1
                    Opcode.PUSH1.getCode(), 2,     // PUSH1 2
                    Opcode.PUSH1.getCode(), 3,     // PUSH1 3
                    Opcode.PUSH1.getCode(), 4,     // PUSH1 4
                    Opcode.SWAP2.getCode()         // SWAP2
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Stack should be [2, 3, 4, 1] (top to bottom)
            Stack<Integer> stack = evm.getStack();
            assertEquals(2, stack.safePop());  // Top
            assertEquals(3, stack.safePop());  // Second
            assertEquals(4, stack.safePop());  // Third
            assertEquals(1, stack.safePop());  // Bottom
        }
    }

    @Nested
    @DisplayName("SWAP3 Tests")
    class Swap3Tests {

        @Test
        @DisplayName("SWAP3 should swap top and fourth elements")
        void testSwap3Basic() {
            // Arrange: Push 10, 20, 30, 40, then SWAP3
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 10,    // PUSH1 10
                    Opcode.PUSH1.getCode(), 20,    // PUSH1 20
                    Opcode.PUSH1.getCode(), 30,    // PUSH1 30
                    Opcode.PUSH1.getCode(), 40,    // PUSH1 40
                    Opcode.SWAP3.getCode()         // SWAP3
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Stack should be [10, 30, 20, 40] (top to bottom)
            Stack<Integer> stack = evm.getStack();
            assertEquals(10, stack.safePop());  // Top
            assertEquals(30, stack.safePop());  // Second
            assertEquals(20, stack.safePop());  // Third
            assertEquals(40, stack.safePop());  // Bottom
        }
    }

    @Nested
    @DisplayName("SWAP4 Tests")
    class Swap4Tests {

        @Test
        @DisplayName("SWAP4 should swap top and fifth elements")
        void testSwap4Basic() {
            // Arrange: Push 1, 2, 3, 4, 5, then SWAP4
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 1,     // PUSH1 1
                    Opcode.PUSH1.getCode(), 2,     // PUSH1 2
                    Opcode.PUSH1.getCode(), 3,     // PUSH1 3
                    Opcode.PUSH1.getCode(), 4,     // PUSH1 4
                    Opcode.PUSH1.getCode(), 5,     // PUSH1 5
                    Opcode.SWAP4.getCode()         // SWAP4
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            evm.printStack();

            // Assert: Stack should be [1, 4, 3, 2, 5] (top to bottom)
            Stack<Integer> stack = evm.getStack();
            assertEquals(1, stack.safePop());  // Top
            assertEquals(4, stack.safePop());  // Second
            assertEquals(3, stack.safePop());  // Third
            assertEquals(2, stack.safePop());  // Fourth
            assertEquals(5, stack.safePop());  // Bottom
        }
    }

    @Nested
    @DisplayName("Parameterized SWAP Tests")
    class ParameterizedSwapTests {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16})
        @DisplayName("SWAP operations should consume correct gas")
        void testSwapGasCost(int swapIndex) {
            // Arrange: Create bytecode that pushes enough values and performs SWAP
            List<Byte> bytecodeList = new ArrayList<>();

            // Push enough values (swapIndex + 1) onto stack
            for (int i = 1; i <= swapIndex + 1; i++) {
                bytecodeList.add(Opcode.PUSH1.getCode());
                bytecodeList.add((byte) i);
            }

            // Add SWAP operation
            bytecodeList.add(getSwapOpcode(swapIndex).getCode());

            byte[] bytecode = toByteArray(bytecodeList);
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Check total gas consumed
            int expectedGasCost = 0;
            // Calculate gas for PUSH operations
            for (int i = 1; i <= swapIndex + 1; i++) {
                expectedGasCost += Opcode.PUSH1.getGasCost();
            }
            // Add gas for SWAP operation
            expectedGasCost += getSwapOpcode(swapIndex).getGasCost();

            assertEquals(expectedGasCost, evm.totalGasUsed());
        }
    }

    @Nested
    @DisplayName("SWAP Edge Cases")
    class SwapEdgeCases {

        @Test
        @DisplayName("SWAP with zero values")
        void testSwapWithZeroValues() {
            // Arrange: Push 0, 0, then SWAP1
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 0,     // PUSH1 0
                    Opcode.PUSH1.getCode(), 0,     // PUSH1 0
                    Opcode.SWAP1.getCode()         // SWAP1
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Both values should still be 0
            assertEquals(0, evm.peek());
            evm.getStack().safePop();
            assertEquals(0, evm.peek());
        }

        @Test
        @DisplayName("SWAP with identical values")
        void testSwapWithIdenticalValues() {
            // Arrange: Push 42, 42, 42, then SWAP2
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 42,    // PUSH1 42
                    Opcode.PUSH1.getCode(), 42,    // PUSH1 42
                    Opcode.PUSH1.getCode(), 42,    // PUSH1 42
                    Opcode.SWAP2.getCode()         // SWAP2
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: All values should still be 42
            Stack<Integer> stack = evm.getStack();
            assertEquals(42, stack.safePop());
            assertEquals(42, stack.safePop());
            assertEquals(42, stack.safePop());
        }
    }

    @Nested
    @DisplayName("SWAP Integration Tests")
    class SwapIntegrationTests {

        @Test
        @DisplayName("Multiple consecutive SWAP operations")
        void testConsecutiveSwapOperations() {
            // Arrange: Push 10, 20, 30, then SWAP1 followed by SWAP2
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 10,    // PUSH1 10
                    Opcode.PUSH1.getCode(), 20,    // PUSH1 20
                    Opcode.PUSH1.getCode(), 30,    // PUSH1 30
                    Opcode.SWAP1.getCode(),        // SWAP1: [20, 30, 10]
                    Opcode.SWAP2.getCode()         // SWAP2: [10, 30, 20]
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Final stack should be [10, 30, 20] (top to bottom)
            Stack<Integer> stack = evm.getStack();
            assertEquals(10, stack.safePop());
            assertEquals(30, stack.safePop());
            assertEquals(20, stack.safePop());
        }

        @Test
        @DisplayName("SWAP with DUP operations")
        void testSwapWithDupOperations() {
            // Arrange: Push 10, 20, DUP1, SWAP1
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 10,    // PUSH1 10
                    Opcode.PUSH1.getCode(), 20,    // PUSH1 20
                    Opcode.DUP1.getCode(),         // DUP1: [20, 20, 10]
                    Opcode.SWAP1.getCode()         // SWAP1: [20, 20, 10]
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Stack should be [20, 20, 10] (top to bottom)
            Stack<Integer> stack = evm.getStack();
            assertEquals(20, stack.safePop());
            assertEquals(20, stack.safePop());
            assertEquals(10, stack.safePop());
        }

        @Test
        @DisplayName("Complex SWAP sequence")
        void testComplexSwapSequence() {
            // Arrange: Push 1,2,3,4,5 then SWAP2, SWAP3, SWAP1
            byte[] bytecode = {
                    Opcode.PUSH1.getCode(), 1,     // PUSH1 1
                    Opcode.PUSH1.getCode(), 2,     // PUSH1 2
                    Opcode.PUSH1.getCode(), 3,     // PUSH1 3
                    Opcode.PUSH1.getCode(), 4,     // PUSH1 4
                    Opcode.PUSH1.getCode(), 5,     // PUSH1 5
                    Opcode.SWAP2.getCode(),        // SWAP2: [3, 4, 5, 2, 1]
                    Opcode.SWAP3.getCode(),        // SWAP3: [2, 4, 5, 3, 1]
                    Opcode.SWAP1.getCode()         // SWAP1: [4, 2, 5, 3, 1]
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Verify final stack state
            Stack<Integer> stack = evm.getStack();
            assertEquals(5, stack.size());
            assertEquals(4, stack.safePop());  // Top
            assertEquals(2, stack.safePop());
            assertEquals(5, stack.safePop());
            assertEquals(3, stack.safePop());
            assertEquals(1, stack.safePop());  // Bottom
        }
    }

    @Nested
    @DisplayName("SWAP with PUSH variants")
    class SwapWithPushVariants {

        @Test
        @DisplayName("SWAP with PUSH2")
        void testSwapWithPush2() {
            // Arrange: Use PUSH2 to push larger values
            byte[] bytecode = {
                    Opcode.PUSH2.getCode(), 0x01, 0x00,  // PUSH2 256
                    Opcode.PUSH2.getCode(), 0x02, 0x00,  // PUSH2 512
                    Opcode.SWAP1.getCode()               // SWAP1
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Stack should have [256, 512] (top to bottom)
            assertEquals(256, evm.peek());
            evm.getStack().safePop();
            assertEquals(512, evm.peek());
        }

        @Test
        @DisplayName("SWAP with PUSH3")
        void testSwapWithPush3() {
            // Arrange: Use PUSH3 to push even larger values
            byte[] bytecode = {
                    Opcode.PUSH3.getCode(), 0x01, 0x00, 0x00,  // PUSH3 65536
                    Opcode.PUSH3.getCode(), 0x02, 0x00, 0x00,  // PUSH3 131072
                    Opcode.SWAP1.getCode()                      // SWAP1
            };
            SimpleEVM evm = new SimpleEVM(bytecode, INITIAL_GAS, "OriginAddress");

            // Act
            evm.run();

            // Assert: Stack should have [65536, 131072] (top to bottom)
            assertEquals(65536, evm.peek());
            evm.getStack().safePop();
            assertEquals(131072, evm.peek());
        }
    }
}