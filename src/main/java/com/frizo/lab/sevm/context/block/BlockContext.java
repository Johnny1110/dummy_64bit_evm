package com.frizo.lab.sevm.context.block;

import com.frizo.lab.sevm.blockchain.Blockchain;
import com.frizo.lab.sevm.common.Address;
import lombok.Getter;

import java.util.UUID;

@Getter
public class BlockContext {

    private final Address coinbase; // Provides information for COINBASE
    private final long gasLimit;  // Provides information for GASLIMIT
    private final long blockNumber; // Provides information for NUMBER
    private final long timestamp;
    private final long difficulty;
    private final long baseFee; // Provides information for BASEFEE (0 if vm runs with NoBaseFee flag and 0 gas price)
    private final long blobBaseFee; // Provides information for BLOBBASEFEE (0 if vm runs with NoBaseFee flag and 0 blob gas price)

    private final String random = UUID.randomUUID().toString(); // Provides information for PREVRANDAO

    public BlockContext(Address coinbase, long gasLimit, long blockNumber, long timestamp) {
        this.coinbase = coinbase;
        this.gasLimit = gasLimit;
        this.blockNumber = blockNumber;
        this.timestamp = timestamp;
        this.difficulty = 1; // Default difficulty, can be adjusted based on consensus rules
        this.baseFee = 0; // Base fee for the block, can be adjusted based on network conditions
        this.blobBaseFee = 0; // Blob base fee, if applicable
    }

    public BlockContext(Blockchain blockchain, long defaultGasLimit) {
        this.coinbase = blockchain.getCoinbase(); // Fetch coinbase from the blockchain
        this.blockNumber = blockchain.getBlockNumber(); // Simulating block number as seconds since epoch
        this.gasLimit = defaultGasLimit;
        this.timestamp = blockchain.getTime();
        this.difficulty = blockchain.getDiff();
        this.baseFee = 0;
        this.blobBaseFee = 0;
    }
}
