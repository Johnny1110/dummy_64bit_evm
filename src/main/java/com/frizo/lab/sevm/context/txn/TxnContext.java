package com.frizo.lab.sevm.context.txn;

import com.frizo.lab.sevm.blockchain.Blockchain;
import com.frizo.lab.sevm.common.Address;
import lombok.Getter;

@Getter
public class TxnContext {

    private final Address txOrigin; // Provides information for ORIGIN
    private final double gasPrice; // Provides information for GASPRICE (and is used to zero the basefee if NoBaseFee is set)
    private final byte[] blockHash; // Provides information for BLOBHASH
    private final double blobFeeCap; // Is used to zero the blobbasefee if NoBaseFee is set
    private final Object accessEvents; // Capture all state accesses for this tx

    public TxnContext(Address txOrigin, long gasPrice, byte[] blockHash, long blobFeeCap, Object accessEvents) {
        this.txOrigin = txOrigin;
        this.gasPrice = gasPrice;
        this.blockHash = blockHash;
        this.blobFeeCap = blobFeeCap;
        this.accessEvents = accessEvents;
    }

    public TxnContext(Blockchain blockchain, Address txOrigin) {
        this.txOrigin = txOrigin;
        this.gasPrice = blockchain.getGasPrice(); // Fetch gas price from the blockchain
        this.blockHash = blockchain.getBlockHash(); // Default block hash, can be adjusted based on consensus rules
        this.blobFeeCap = blockchain.getBlobFeeCap(); // Default blob fee cap, can be adjusted based on network conditions
        this.accessEvents = null; // Placeholder for access events, can be implemented later
    }
}
