package com.frizo.lab.sevm.blockchain.impl;

import com.frizo.lab.sevm.blockchain.Blockchain;
import lombok.Getter;

public class BlockChainFactory {

    @Getter
    private static final Blockchain blockchainInstance = new MockBlockChain();

}
