package com.frizo.lab.sevm.common;

import com.frizo.lab.sevm.utils.NumUtils;
import lombok.Getter;

@Getter
public class Address {

    private String addressString;
    private byte[] addressBytes;
    private long addressLong;

    private Address() {}

    public static Address of(String address) {
        validateAddress(address);
        Address addr = new Address();
        addr.addressBytes = NumUtils.hexStringToBytes(address);
        addr.addressLong = NumUtils.bytesToLong(addr.addressBytes);
        addr.addressString = NumUtils.bytesToHex(addr.addressBytes);
        return addr;
    }

    public static Address of(byte[] address) {
        validateAddress(address);
        Address addr = new Address();
        addr.addressBytes = address;
        addr.addressString = NumUtils.bytesToHex(address);
        addr.addressLong = NumUtils.bytesToLong(addr.addressBytes);
        return addr;
    }

    public static Address of(long address) {
        Address addr = new Address();
        addr.addressLong = address;
        addr.addressBytes = NumUtils.longToBytesWithPadding(address, 8);
        addr.addressString = NumUtils.bytesToHex(addr.addressBytes);
        return addr;
    }

    public static void validateAddress(String address) {
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }

        // address must start with '0x' and be 18 characters long
        if (!address.startsWith("0x") || address.length() != 18) {
            throw new IllegalArgumentException("Invalid address format. Must be '0x' followed by 16 hex characters.");
        }
    }

    public static void validateAddress(byte[] address) {
        if (address == null || address.length != 8) {
            throw new IllegalArgumentException("Address must be 20 bytes long");
        }
    }

    @Override
    public String toString() {
        return addressString;
    }
}
