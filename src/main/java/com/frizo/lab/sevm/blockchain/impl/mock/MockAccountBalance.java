package com.frizo.lab.sevm.blockchain.impl.mock;

import com.frizo.lab.sevm.common.Address;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MockAccountBalance {

    private static final Map<Address, Long> accountBalances = new HashMap<>();

    static {
        log.info("[MockBlockChain] Initializing MockAccountBalance");
        // 預設一些帳戶餘額
        accountBalances.put(Address.of("0x0111111111111111"), 1000000000L); // 1 ETH
        accountBalances.put(Address.of("0x0222222222222222"), 500000000L);  // 0.5 ETH
        accountBalances.put(Address.of("0x0333333333333333"), 300000000L);  // 0.3 ETH
    }

    public static long getBalance(Address hexAddress) {
        log.info("[MockAccountBalance] Getting balance for address: {}", hexAddress);
        return accountBalances.getOrDefault(hexAddress, 0L);
    }

    public static void transfer(Address from, Address to, long amount) {
        log.info("[MockAccountBalance] Transferring {} wei from {} to {}", amount, from, to);
        long fromBalance = accountBalances.getOrDefault(from, 0L);
        if (fromBalance < amount) {
            log.error("[MockAccountBalance] Insufficient balance for transfer from {}: current balance is {}", from, fromBalance);
            throw new IllegalArgumentException("Insufficient balance for transfer");
        }
        accountBalances.put(from, fromBalance - amount);
        accountBalances.put(to, accountBalances.getOrDefault(to, 0L) + amount);
        log.info("[MockAccountBalance] Transfer complete. New balances: {} -> {}, {} -> {}", from, accountBalances.get(from), to, accountBalances.get(to));
    }

}
