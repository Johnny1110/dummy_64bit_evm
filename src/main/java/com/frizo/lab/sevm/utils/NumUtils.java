package com.frizo.lab.sevm.utils;

import java.util.List;

public class NumUtils {

    /**
     * int to bytes4（EVM style：right align）
     */
    public static byte[] intTo4Bytes(int value) {
        byte[] result = new byte[4];
        result[0] = (byte) ((value >> 24) & 0xFF);
        result[1] = (byte) ((value >> 16) & 0xFF);
        result[2] = (byte) ((value >> 8) & 0xFF);
        result[3] = (byte) (value & 0xFF);
        return result;
    }

    /**
     * read 4 bytes, convert to int
     */
    public static int bytes4ToInt(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Must be exactly 4 bytes");
        }
        return ((bytes[0] & 0xFF) << 24)
                | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8)
                | (bytes[3] & 0xFF);
    }

    /**
     * Alternative implementation using your existing padLeft approach
     * This matches your current code style better
     */
    public static int bytesToInt(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0;
        }

        if (bytes.length > 4) {
            throw new IllegalArgumentException("Input must be 4 bytes or less, got " + bytes.length + " bytes");
        }

        // Pad left to 4 bytes if needed
        if (bytes.length < 4) {
            bytes = padLeft(bytes, 4);
        }

        return bytes4ToInt(bytes);
    }

    private static byte[] padLeft(byte[] bytes, int targetLength) {
        if (bytes.length >= targetLength) {
            return bytes;
        }

        byte[] padded = new byte[targetLength];
        // Copy original bytes to the right side (big-endian padding)
        System.arraycopy(bytes, 0, padded, targetLength - bytes.length, bytes.length);
        // Left side automatically filled with zeros
        return padded;
    }

    public static String bytesToHex(byte[] value) {
        StringBuilder sb = new StringBuilder();
        for (byte b : value) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static String intToHex(int contractAddress) {
        String hex = Integer.toHexString(contractAddress);
        if (hex.length() % 2 != 0) {
            hex = "0" + hex; // Ensure even length
        }
        return "0x" + hex;
    }


    /**
     * Convert stack byte array back to original integer
     * Stack stores bytes in reverse order (little-endian style)
     *
     * @param ints List of integers representing bytes from stack (in reverse order)
     * @return Original integer value
     */
    public static int stackIntArrayToInt(List<Integer> ints) {
        if (ints == null || ints.isEmpty()) {
            return 0;
        }

        if (ints.size() > 4) {
            throw new IllegalArgumentException("Cannot convert more than 4 bytes to int");
        }

        int result = 0;
        int size = ints.size();

        // Process bytes in reverse order since stack stores them reversed
        for (int i = 0; i < size; i++) {
            int byteValue = ints.get(i) & 0xFF; // Ensure it's treated as unsigned byte
            result |= (byteValue << (i * 8));   // Shift left by i*8 bits
        }

        return result;
    }

    public static void main(String[] args) {
//        List<Integer> stackInts = List.of(120, 86, 52, 18);
//        int stackIntValue = stackIntArrayToInt(stackInts);
//        System.out.println("Stack int array to int: " + stackIntValue);
//
//        int val = 0x61626364; // 'a', 'b', 'c', 'd'
//        System.out.println(intToString(val)); // Output: abcd
//
//        val = 0x00000061;
//        System.out.println(intToString(val)); // Output: ���a (前三個是 0x00)
//
//        val = 0x61626364; // 'a', 'b', 'c', 'd'
//        System.out.println(intToTrimmedString(val)); // Output: abcd
//
//        val = 0x00000061;
//        System.out.println(intToTrimmedString(val)); // Output: a

        var bytes = longToBytesWithPadding(99999999L, 8);
        for (byte aByte : bytes) {
            System.out.println("Byte: " + String.format("%02X", aByte));
        }

        var recoveredLong = paddingBytesToLong(bytes, 8);
        System.out.println("Recovered long: " + recoveredLong); // Should print 99999999

        long val = 0x61626364; // 'a', 'b', 'c', 'd'
        System.out.println(longToTrimmedString(val)); // Output: abcd
    }


    // assumes input is a byte array each 4 bytes as a unit
    // example [0x00, 0x00, 0x00, 0x45, 0x00, 0x00, 0x00, 0x72] represents "er"
    public static String bytes4ToString(byte[] bytesWithPadding) {
        if (bytesWithPadding == null || bytesWithPadding.length == 0) {
            return "";
        }

        // Ensure the array length is divisible by 4
        if (bytesWithPadding.length % 4 != 0) {
            throw new IllegalArgumentException("Byte array length must be divisible by 4");
        }

        StringBuilder result = new StringBuilder();

        // Process every 4 bytes
        for (int i = 0; i < bytesWithPadding.length; i += 4) {
            // Convert 4 bytes to int (big-endian)
            int value = ((bytesWithPadding[i] & 0xFF) << 24) |
                    ((bytesWithPadding[i + 1] & 0xFF) << 16) |
                    ((bytesWithPadding[i + 2] & 0xFF) << 8) |
                    (bytesWithPadding[i + 3] & 0xFF);

            // Convert to character and append
            result.append((char) value);
        }

        return result.toString();
    }

    public static String bytes8ToString(byte[] bytesWithPadding) {
        if (bytesWithPadding == null || bytesWithPadding.length == 0) {
            return "";
        }

        // Ensure the array length is exactly 8
        if (bytesWithPadding.length != 8) {
            throw new IllegalArgumentException("Byte array length must be exactly 8");
        }

        StringBuilder result = new StringBuilder();

        // Convert each byte to character
        for (int i = 0; i < bytesWithPadding.length; i++) {
            result.append((char) (bytesWithPadding[i] & 0xFF));
        }

        return result.toString();
    }

    /**
     * Convert a string to a byte array, where each character is represented by 4 bytes.
     * This is useful for EVM-style encoding where each character is padded to 4 bytes (left padded).
     * For example: 'A' -> [0x00, 0x00, 0x00, 0x41]
     *
     * @param text the input string to convert
     * @return a byte array where each character is represented by 4 bytes
     */
    public static byte[] stringToBytes(String text) {
        if (text == null || text.isEmpty()) return new byte[0];

        byte[] result = new byte[text.length() * 4];
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 填入每個 char 的 4-byte big-endian 表示
            result[i * 4] = 0x00;
            result[i * 4 + 1] = 0x00;
            result[i * 4 + 2] = 0x00;
            result[i * 4 + 3] = (byte) c;
        }
        return result;
    }

    public static byte[] stringTo8Bytes(String text) {
        if (text == null || text.isEmpty()) return new byte[8];

        byte[] result = new byte[8];
        for (int i = 0; i < text.length() && i < 8; i++) {
            char c = text.charAt(i);
            // 填入每個 char 的 4-byte big-endian 表示
            result[i] = (byte) c;
        }
        return result;
    }

    public static String intToString(int value) {
        byte[] bytes = intTo4Bytes(value);
        return new String(bytes);
    }

    public static String intToTrimmedString(int value) {
        byte[] bytes = intTo4Bytes(value);

        int start = 0;
        while (start < bytes.length && bytes[start] == 0) {
            start++;
        }

        if (start == bytes.length) {
            return "";
        }

        return new String(bytes, start, bytes.length - start);
    }

    public static String longToTrimmedString(long value) {
        byte[] bytes = longToBytesWithPadding(value, 8);

        int start = 0;
        while (start < bytes.length && bytes[start] == 0) {
            start++;
        }

        if (start == bytes.length) {
            return "";
        }

        return new String(bytes, start, bytes.length - start);
    }

    public static byte[] longToBytesWithPadding(Long value, int maxPadding) {
        if (value == null) {
            return new byte[maxPadding];
        }
        if (maxPadding <= 0 || maxPadding > 8) {
            throw new IllegalArgumentException("maxLength must be between 1 and 8");
        }

        byte[] bytes = new byte[maxPadding];
        for (int i = 0; i < maxPadding; i++) {
            bytes[maxPadding - 1 - i] = (byte) (value >> (i * 8));
        }

        bytes = padLeft(bytes, maxPadding);
        return bytes;
    }

    public static Long paddingBytesToLong(byte[] bytes, int length) {
        if (bytes == null || bytes.length < length) {
            throw new IllegalArgumentException("Input bytes must not be null and must have at least " + length + " bytes");
        }

        if (length > 8) {
            throw new IllegalArgumentException("Length must not exceed 8 bytes");
        }

        // Ensure the byte array is exactly 'length' bytes long
        byte[] paddedBytes = padLeft(bytes, length);

        long result = 0;
        for (int i = 0; i < length; i++) {
            result |= ((long) (paddedBytes[i] & 0xFF)) << ((length - 1 - i) * 8);
        }
        return result;
    }

    public static String byteToHex(Byte value) {
        if (value == null) {
            return "00"; // Return "00" for null values
        }
        return String.format("%02X", value); // Format byte as two-digit hex
    }
}
