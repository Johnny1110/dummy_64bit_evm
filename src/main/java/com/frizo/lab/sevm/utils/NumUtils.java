package com.frizo.lab.sevm.utils;

public class NumUtils {

    /**
     *  int to bytes4（EVM style：right align）
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
                |  (bytes[3] & 0xFF);
    }

    public static int bytesToInt(byte[] bytes) {
        if (bytes.length > 4) {
            throw new IllegalArgumentException("Input must lower than 4 bytes");
        }
        bytes = padLeft(bytes, 4);
        return bytes4ToInt(bytes);
    }

    private static byte[] padLeft(byte[] bytes, int i) {
        if (bytes.length >= i) {
            return bytes;
        }
        byte[] padded = new byte[i];
        System.arraycopy(bytes, 0, padded, i - bytes.length, bytes.length);
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
        return hex;
    }
}
