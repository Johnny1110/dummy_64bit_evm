package com.frizo.lab.sevm.blockchain.impl;

import com.frizo.lab.sevm.blockchain.Blockchain;
import com.frizo.lab.sevm.blockchain.impl.mock.MockAccountBalance;
import com.frizo.lab.sevm.blockchain.impl.mock.MockContractStorage;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.op.Opcode;
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
}
