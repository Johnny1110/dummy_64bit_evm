package com.frizo.lab.sevm.blockchain.impl;

import com.frizo.lab.sevm.blockchain.Blockchain;
import com.frizo.lab.sevm.blockchain.impl.mock.MockAccountBalance;
import com.frizo.lab.sevm.blockchain.impl.mock.MockContractStorage;
import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MockBlockChain implements Blockchain {

    @Override
    public byte[] loadCode(String contractAddress) throws EVMException.ContractNotFoundException {
        log.info("[MockBlockChain] Loading contract code for address: {}", contractAddress);
        byte[] code = MockContractStorage.get(contractAddress);
        if (code == null) {
            log.error("[MockBlockChain] Contract not found at address: {}", contractAddress);
            throw new EVMException.ContractNotFoundException("Contract not found at address: " + contractAddress);
        }
        return code;
    }

    @Override
    public void transfer(String from, String to, long value) {
        log.info("[MockBlockChain] Transfer ETH:[{}] from [{}] to [{}]", value, from, to);
        try {
            MockAccountBalance.transfer(from, to, value);
        } catch (Exception e) {
            throw  new EVMException.ValueTransferException("Transfer failed from " + from + " to " + to + " with value: " + value, e.getMessage());
        }
    }

    @Override
    public void registerContract(String contractAddress, byte[] contractBytecode) {
        if (MockContractStorage.exists(contractAddress)) {
            log.warn("[MockBlockChain] Contract already registered at address: {}", contractAddress);
            throw new EVMException.ContractAlreadyExistsException("Contract already exists at address: " + contractAddress);
        } else {
            MockContractStorage.addContract(contractAddress, contractBytecode);
            log.info("[MockBlockChain] Registered contract at address: {}", contractAddress);
        }
    }

    @Override
    public long balance(String hexAddress) {
        return MockAccountBalance.getBalance(hexAddress);
    }

    @Override
    public double getGasPrice() {
        // random 0.5 ~ 1 mock real Ethereum gas price
        return 0.5 + Math.random() * 0.5;
    }

    @Override
    public byte[] getBlockHash() {
        // random 8 bytes as block hash
        return NumUtils.randomBytes(8);
    }

    @Override
    public double getBlobFeeCap() {
        return 0.0; // Mock blob fee cap, can be adjusted based on network conditions
    }

    @Override
    public long getBlockNumber() {
        return System.currentTimeMillis() / 1000; // Simulating block number as seconds since epoch
    }

    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }

    @Override
    public long getDiff() {
        return 1; // Default difficulty, can be adjusted based on consensus rules
    }

    @Override
    public Address getCoinbase() {
        return Address.of("0x123456789011223D");
    }
}
