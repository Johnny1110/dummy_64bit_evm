package com.frizo.lab.sevm.blockchain.impl.mock;

import com.frizo.lab.sevm.common.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MockAccount {

    public static Object getCodeHash(Address creationAddress) {
        if (!accounts.containsKey(creationAddress)) {
            return null;
        }
        log.info("[MockAccount] Getting code hash for address: {}", creationAddress);
        return new Object(); // Mock implementation, returning a dummy object as code hash
    }

    @Data
    @AllArgsConstructor
    private static class AccInfo {
        private Address address;
        private long balance;
        private long nonce;

        public void addBalance(long amount) {
            this.balance += amount;
        }

        public void subtractBalance(long amount) {
            if (this.balance < amount) {
                log.error("[MockAccount] Insufficient balance for address {}: current balance is {}", address, this.balance);
                throw new IllegalArgumentException("Insufficient balance for address " + address);
            }
            this.balance -= amount;
        }
    }

    private static final Map<Address, AccInfo> accounts = new HashMap<>();

    static {
        log.info("[MockBlockChain] Initializing MockAccountBalance");
        // 預設一些帳戶餘額
        accounts.put(Address.of("0x1234567890abcdef"), new AccInfo(Address.of("0x1234567890abcdef"), 10000000000L, 1)); // 10 ETH
        accounts.put(Address.of("0x0111111111111111"), new AccInfo(Address.of("0x0111111111111111"), 1000000000L, 1)); // 1 ETH
        accounts.put(Address.of("0x0222222222222222"), new AccInfo(Address.of("0x0222222222222222"), 500000000L, 1));  // 0.5 ETH
        accounts.put(Address.of("0x0333333333333333"), new AccInfo(Address.of("0x0333333333333333"), 300000000L, 1));  // 0.3 ETH
    }

    public static void create(Address creationAddress) {
        log.info("[MockAccount] Creating account for address: {}", creationAddress);
        if (accounts.containsKey(creationAddress)) {
            log.warn("[MockAccount] Account already exists for address: {}", creationAddress);
            throw new IllegalArgumentException("Account already exists for address: " + creationAddress);
        }
        accounts.put(creationAddress, new AccInfo(creationAddress, 0L, 1L));
        log.info("[MockAccount] Account created successfully for address: {}", creationAddress);
    }

    public static void setNonce(Address from, int number) {
        log.info("[MockAccount] Setting nonce for address {} to {}", from, number);
        AccInfo accInfo = accounts.get(from);
        if (accInfo == null) {
            log.error("[MockAccount] Address {} not found", from);
            throw new IllegalArgumentException("Address not found: " + from);
        }
        accInfo.nonce = number;
    }

    public static long getBalance(Address hexAddress) {
        log.info("[MockAccountBalance] Getting balance for address: {}", hexAddress);
        return accounts.getOrDefault(hexAddress, new AccInfo(hexAddress, 0L, 0L)).balance;
    }

    public static void transfer(Address from, Address to, long amount) {
        log.info("[MockAccountBalance] Transferring {} wei from {} to {}", amount, from, to);
        long fromBalance = accounts.getOrDefault(from, new AccInfo(from, 0L, 0L)).balance;
        if (fromBalance < amount) {
            log.error("[MockAccountBalance] Insufficient balance for transfer from {}: current balance is {}", from, fromBalance);
            throw new IllegalArgumentException("Insufficient balance for transfer");
        }

        accounts.get(from).subtractBalance(amount);
        accounts.computeIfAbsent(to, k -> new AccInfo(k, 0L, 1L)).addBalance(amount);
        log.info("[MockAccountBalance] Transfer successful: {} wei from {} to {}", amount, from, to);
    }

    public static long getNonce(Address creationAddress) {
        return accounts.getOrDefault(creationAddress, new AccInfo(creationAddress, 0L, 0)).nonce;
    }
}
