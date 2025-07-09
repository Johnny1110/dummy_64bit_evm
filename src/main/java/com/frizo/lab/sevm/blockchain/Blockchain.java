package com.frizo.lab.sevm.blockchain;

import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.exception.EVMException;

public interface Blockchain {

    byte[] loadCode(String contractAddress) throws EVMException.ContractNotFoundException;
    void transfer(String from, String to, long value);

    void registerContract(String contractAddress, byte[] contractBytecode);

    long balance(String hexAddress);

    double getGasPrice();

    byte[] getBlockHash();

    double getBlobFeeCap();

    long getBlockNumber();

    long getTime();

    long getDiff();

    Address getCoinbase();
}
