package com.frizo.lab.sevm.blockchain.impl;

import com.frizo.lab.sevm.blockchain.Blockchain;
import com.frizo.lab.sevm.blockchain.impl.mock.MockAccount;
import com.frizo.lab.sevm.blockchain.impl.mock.MockContractStorage;
import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockBlockChain implements Blockchain {

    @Override
    public byte[] loadCode(Address contractAddress) throws EVMException.ContractNotFoundException {
        log.info("[MockBlockChain] Loading contract code for address: {}", contractAddress);
        byte[] code = MockContractStorage.get(contractAddress);
        if (code == null) {
            log.error("[MockBlockChain] Contract not found at address: {}", contractAddress);
            throw new EVMException.ContractNotFoundException("Contract not found at address: " + contractAddress);
        }
        return code;
    }

    @Override
    public void transfer(Address from, Address to, long value) {
        log.info("[MockBlockChain] Transfer ETH:[{}] from [{}] to [{}]", value, from, to);
        try {
            MockAccount.transfer(from, to, value);
        } catch (Exception e) {
            throw new EVMException.ValueTransferException("Transfer failed from " + from + " to " + to + " with value: " + value, e.getMessage());
        }
    }

    @Override
    public void registerContract(Address contractAddress, byte[] contractBytecode) {
        if (MockContractStorage.exists(contractAddress)) {
            log.warn("[MockBlockChain] Contract already registered at address: {}", contractAddress);
            throw new EVMException.ContractAlreadyExistsException("Contract already exists at address: " + contractAddress);
        } else {
            MockContractStorage.addContract(contractAddress, contractBytecode);
            log.info("[MockBlockChain] Registered contract at address: {}", contractAddress);
        }
    }

    @Override
    public long balance(Address address) {
        return MockAccount.getBalance(address);
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

    @Override
    public boolean canTransfer(Address from, long value) {
        return true;
    }

    @Override
    public Address calculateNewContractAddress(Address from) {
        // random a long value as nonce
        long nonce = System.currentTimeMillis(); // Simulating nonce
        return Address.of(nonce);
    }

    @Override
    public boolean exist(Address creationAddress) {
        return MockContractStorage.exists(creationAddress);
    }

    @Override
    public void createContract(Address creationAddress) {
        MockAccount.create(creationAddress);
    }

    @Override
    public int getNonce(Address creationAddress) {
        return (int) MockAccount.getNonce(creationAddress);
    }

    @Override
    public Object getCodeHash(Address creationAddress) {
        return MockAccount.getCodeHash(creationAddress);
    }

    @Override
    public void setNonce(Address from, int number) {
        log.info("[MockBlockChain] Setting nonce for address {} to {}", from, number);
        MockAccount.setNonce(from, number);
    }

    @Override
    public long takeSnapshot() {
        return 1; // Mock snapshot, always returns 1
    }

    @Override
    public void revertToSnapshot(long snapshot) {
        log.info("[MockBlockChain] Reverting to snapshot: {}", snapshot);
        // In a real implementation, this would restore the state to the snapshot
        // For mock, we do nothing as we don't maintain state history
    }

    @Override
    public void setCode(Address creationAddress, byte[] deployedCode) {
        log.info("[MockBlockChain] Setting code for contract at address: {}", creationAddress);
        MockContractStorage.addContract(creationAddress, deployedCode);
        log.info("[MockBlockChain] Code set successfully for contract at address: {}", creationAddress);
    }
}
