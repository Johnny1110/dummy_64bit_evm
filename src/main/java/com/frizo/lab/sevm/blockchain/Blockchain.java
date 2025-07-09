package com.frizo.lab.sevm.blockchain;

import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.exception.EVMException;

public interface Blockchain {

    byte[] loadCode(Address contractAddress) throws EVMException.ContractNotFoundException;
    void transfer(Address from, Address to, long value);

    void registerContract(Address contractAddress, byte[] contractBytecode);

    long balance(Address hexAddress);

    double getGasPrice();

    byte[] getBlockHash();

    double getBlobFeeCap();

    long getBlockNumber();

    long getTime();

    long getDiff();

    Address getCoinbase();
}
