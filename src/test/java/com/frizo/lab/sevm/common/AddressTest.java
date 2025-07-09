package com.frizo.lab.sevm.common;

import com.frizo.lab.sevm.utils.NumUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void testOfString() {
        Address addr = Address.of("0x1234567890ABCDEF");
        assertEquals("0x1234567890ABCDEF", addr.getAddressString());
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, addr.getAddressBytes());
        assertEquals(0x1234567890abcdefL, addr.getAddressLong());
        System.out.println(addr);
    }

    @Test
    void testBytes() {
        Address addr = Address.of(new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef});
        assertEquals("0x1234567890ABCDEF", addr.getAddressString());
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, addr.getAddressBytes());
        assertEquals(0x1234567890abcdefL, addr.getAddressLong());
        System.out.println(addr);

        byte[] bs = NumUtils.hexStringToBytes(addr.getAddressString());
        for (byte b : bs) {
            System.out.println(NumUtils.byteToHex(b));
        }
    }

    @Test
    void testOfLong() {
        Address addr = Address.of(0x1234567890abcdefL);
        assertEquals("0x1234567890ABCDEF", addr.getAddressString());
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef}, addr.getAddressBytes());
        assertEquals(0x1234567890abcdefL, addr.getAddressLong());
        System.out.println(addr);
    }
}