package com.frizo.lab.sevm.context.log;

import com.frizo.lab.sevm.common.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LogEntry {

    private Address contractAddress; //current frame contractAddress
    private List<Long> topics;   // max 4 topic, usually: event signature + indexed params
    private byte[] data;            //non-indexed data (from memory)

    private long blockNumber;       // optional: block number
    private Address txOrigin;        // optional: txn origin user address
}
