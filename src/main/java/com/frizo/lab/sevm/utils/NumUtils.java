package com.frizo.lab.sevm.utils;

import java.nio.charset.StandardCharsets;
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
        return "0x" + sb.toString();
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
     * Convert a string to a byte array.
     * example: "Error" is [0x45, 0x72, 0x72, 0x6F, 0x72] groupSize = 8
     * group 8 bytes as 1 group padding left, so the output is [ 0x00, 0x00, 0x00, 0x45, 0x72, 0x72, 0x6F, 0x72]
     * @param text plain text to convert
     * @param groupSize the size of each group in bytes
     * @return a byte array representing the string, padded to the specified group size
     */
    public static byte[] stringToBytes(String text, int groupSize) {
        // Convert string to bytes using UTF-8 encoding
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

        // Calculate how many complete groups we need
        int totalGroups = (textBytes.length + groupSize - 1) / groupSize;

        // Create result array with proper size
        byte[] result = new byte[totalGroups * groupSize];

        // Copy original bytes to the end of the result array (left padding with zeros)
        System.arraycopy(textBytes, 0, result, result.length - textBytes.length, textBytes.length);

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

    public static long bytesToLong(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return 0;
        }

        if (bytes.length > 8) {
            throw new IllegalArgumentException("Input must be 8 bytes or less, got " + bytes.length + " bytes");
        }

        // Pad left to 8 bytes if needed
        if (bytes.length < 8) {
            bytes = padLeft(bytes, 8);
        }

        return paddingBytesToLong(bytes, 8);
    }

    public static String longToHex(long value) {
        String hex = Long.toHexString(value);
        if (hex.length() % 2 != 0) {
            hex = "0" + hex; // Ensure even length
        }
        return "0x" + hex;
    }

    /**
     * Convert a byte array to a string representation, grouping bytes into specified sizes.
     * ex: input: rawdata = 000000000000004500000000000000720000000000000072000000000000006F0000000000000072 and  groupSize = 8
     * group rawdata with 8 size: 0000000000000045, 0000000000000072, 0000000000000072, 000000000000006F, 0000000000000072,
     * remove leading zeros:
     *      "0x45, 0x72, 0x72, 0x6F, 0x72"
     * convert to output: "Error"
     * @param rawData the byte array to convert
     * @param groupSize the size of each group in bytes
     * @return a string representation of the byte array, grouped by the specified size
     */
    public static String bytesToString(byte[] rawData, int groupSize) {
        if (rawData == null || rawData.length == 0 || groupSize <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        // Process the byte array in groups of specified size
        for (int i = 0; i < rawData.length; i += groupSize) {
            // Calculate the end index for this group
            int endIndex = Math.min(i + groupSize, rawData.length);

            // Process each byte in the group individually
            for (int j = i; j < endIndex; j++) {
                int byteValue = rawData[j] & 0xFF;

                // Skip zero bytes (leading zeros)
                if (byteValue == 0) {
                    continue;
                }

                // Convert to ASCII character if it's a valid printable character
                if (byteValue >= 32 && byteValue <= 126) {
                    result.append((char) byteValue);
                }
            }
        }

        return result.toString();
    }

    // 輔助方法：將十六進制字符串轉換為字節數組（用於測試）
    public static byte[] hexStringToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }
}
