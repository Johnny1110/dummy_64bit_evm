package com.frizo.lab.sevm.context.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LogEntry {

    private String contractAddress; //current frame contractAddress
    private List<Integer> topics;   // max 4 topic, usually: event signature + indexed params
    private byte[] data;            //non-indexed data (from memory)

    private long blockNumber;       // optional: block number
    private String txOrigin;        // optional: txn origin user address
}
