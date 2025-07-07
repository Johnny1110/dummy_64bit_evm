package com.frizo.lab.sevm.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumUtilsTest {

    @Test
    void test1() {
        // Test with the example from the comment
        // This represents the hex string: 000000000000004500000000000000720000000000000072000000000000006F0000000000000072
        byte[] testData = {
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x45,  // Group 1: 0x45 = 'E'
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x72,  // Group 2: 0x72 = 'r'
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x72,  // Group 3: 0x72 = 'r'
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6F,  // Group 4: 0x6F = 'o'
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x72   // Group 5: 0x72 = 'r'
        };

        String result = NumUtils.bytesToString(testData, 8);
        System.out.println("Result: " + result); // Should output: "Error"
        assertEquals("Error", result);
    }

    @Test
    void test2() {
        // Test with the example from the comment
        // This represents the hex string: 000000000000004500000000000000720000000000000072000000000000006F0000000000000072
        byte[] testData2 = {
                0x00, 0x00, 0x00, 0x45, 0x72, 0x72, 0x6F, 0x72   // 0x45='E', 0x72='r', 0x72='r', 0x6F='o', 0x72='r'
        };

        String result = NumUtils.bytesToString(testData2, 8);
        System.out.println("Result: " + result); // Should output: "Error"
        assertEquals("Error", result);
    }

}