package com.frizo.lab.utils;

public class NumUtils {

    public static byte[] intTo32Bytes(int value) {
        byte[] result = new byte[32];
        // 放在最後4 byte，模擬 int → bytes32
        result[28] = (byte) ((value >> 24) & 0xFF);
        result[29] = (byte) ((value >> 16) & 0xFF);
        result[30] = (byte) ((value >> 8) & 0xFF);
        result[31] = (byte) (value & 0xFF);
        return result;
    }

    public static int bytes32ToInt(byte[] bytes) {
        return ((bytes[28] & 0xFF) << 24) |
                ((bytes[29] & 0xFF) << 16) |
                ((bytes[30] & 0xFF) << 8) |
                (bytes[31] & 0xFF);
    }

}
