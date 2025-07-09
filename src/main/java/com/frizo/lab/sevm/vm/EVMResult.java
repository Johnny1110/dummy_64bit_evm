package com.frizo.lab.sevm.vm;

import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.log.LogEntry;
import com.frizo.lab.sevm.exception.EVMException;
import lombok.Data;

import java.util.List;

@Data
public class EVMResult {

    private boolean success;
    private boolean isReverted;
    private String msg;
    private String revertReason;
    private long gasRemaining;
    private long gasUsed;
    private List<LogEntry> logs;
    // Return data from the execution
    private byte[] returnData;
    private long returnDataSize;

    private Address contractAddress;

    private EVMResult(EVMException ex, EVMContext context) {
        this.success = context.getCurrentFrame().isSuccess();
        this.isReverted = context.getCurrentFrame().isReverted();
        this.revertReason = context.getCurrentFrame().getRevertReason();
        this.msg = ex != null ? ex.getMessage() : "OK";
        this.gasRemaining = context.getGasRemaining();
        this.gasUsed = context.getGasUsed();
        this.returnData = context.getCurrentFrame().getReturnData();
        this.returnDataSize = context.getCurrentFrame().getReturnSize();
        this.contractAddress = context.getCurrentFrame().getContractAddress();
    }

    private EVMResult(EVMException ex, EVMContext context, Address creationAddress) {
        this.success = context.getCurrentFrame().isSuccess();
        this.isReverted = context.getCurrentFrame().isReverted();
        this.revertReason = context.getCurrentFrame().getRevertReason();
        this.msg = ex != null ? ex.getMessage() : "OK";
        this.gasRemaining = context.getGasRemaining();
        this.gasUsed = context.getGasUsed();
        this.returnData = context.getCurrentFrame().getReturnData();
        this.returnDataSize = context.getCurrentFrame().getReturnSize();
        this.contractAddress = creationAddress;
    }

    public static EVMResult created(EVMContext context, Address creationAddress) {
        return new EVMResult(null, context, creationAddress);
    }

    public static EVMResult failed(EVMException e, EVMContext context) {
        return new EVMResult(e, context);
    }

    public static EVMResult OK(EVMContext context) {
        return new EVMResult(null, context);
    }
}
