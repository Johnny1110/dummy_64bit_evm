package com.frizo.lab.sevm.context.call;

import lombok.Getter;

@Getter
public class CallReturnDataBuffer {

    private byte[] returnData;
    private long returnOffset;
    private long returnSize;
    private boolean reverted;
    private String revertReason;

    public void clear() {
        this.returnData = null;
        this.returnOffset = 0;
        this.returnSize = 0;
        this.reverted = false;
        this.revertReason = null;
    }

    public void setReturnData(byte[] returnData, long returnOffset, long returnSize) {
        this.returnData = returnData;
        this.returnOffset = returnOffset;
        this.returnSize = returnSize;
        this.reverted = false; // Reset reverted state when setting return data
        this.revertReason = null; // Reset revert reason when setting return data
    }

    public void setReverted(String reason) {
        this.reverted = true;
        this.revertReason = reason;
        this.returnData = null; // Clear return data on revert
        this.returnOffset = 0; // Reset offset on revert
        this.returnSize = 0; // Reset size on revert
    }

    public byte[] getReturnData(long returnDataOffset, long length) {
        if (returnData == null || returnDataOffset < 0 || returnDataOffset + length > returnSize) {
            throw new IllegalArgumentException("Invalid return data access: offset or length out of bounds");
        }
        byte[] dataToReturn = new byte[(int) length];
        System.arraycopy(returnData, (int) returnDataOffset, dataToReturn, 0, (int) length);
        return dataToReturn;
    }
}
