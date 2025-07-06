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
        List<Integer> stackInts = List.of(120, 86, 52, 18);
        int stackIntValue = stackIntArrayToInt(stackInts);
        System.out.println("Stack int array to int: " + stackIntValue);
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
}
