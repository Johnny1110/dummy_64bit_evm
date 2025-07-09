package com.frizo.lab.sevm.exception;

import com.frizo.lab.sevm.common.Address;
import com.frizo.lab.sevm.op.Opcode;

public class EVMException extends RuntimeException {
    public EVMException(String message) {
        super(message);
    }

    public static class StackUnderflowException extends EVMException {
        public StackUnderflowException() {
            super("Stack underflow: not enough items on the stack");
        }

        public StackUnderflowException(String message) {
            super(message);
        }
    }

    public static class StackOverflowException extends EVMException {
        public StackOverflowException() {
            super("Stack overflow: too many items on the stack");
        }
    }

    public static class OutOfGasException extends EVMException {
        public OutOfGasException() {
            super("Out of gas: not enough gas to execute the operation");
        }
    }

    public static class InvalidJumpException extends EVMException {
        public InvalidJumpException() {
            super("Invalid jump destination: the jump target is not a valid jump destination");
        }
    }

    public static class UnknownOpcodeException extends EVMException {
        public UnknownOpcodeException(Opcode opcode) {
            super("Unknown opcode: " + opcode);
        }
    }

    public static class NoMoreCodeException extends EVMException {
        public NoMoreCodeException() {
            super("No more code to execute: reached the end of the code");
        }
    }

    public static class CallStackOverFlowException extends EVMException {
        public CallStackOverFlowException() {
            super("Call stack overflow: too many nested calls");
        }
    }

    public static class CallStackUnderFlowException extends EVMException {
        public CallStackUnderFlowException() {
            super("Call stack underflow: no frames to pop from the call stack");
        }
    }

    public static class CallInternalException extends EVMException {
        public CallInternalException(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class InvalidStackOperationException extends EVMException {
        public InvalidStackOperationException(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class InvalidMemoryAccess extends EVMException {
        public InvalidMemoryAccess(String returnDataOutOfBounds) {
            super(returnDataOutOfBounds);
        }
    }

    public static class

    ContractNotFoundException extends EVMException {
        public ContractNotFoundException(String contractAddress) {
            super("Contract not found: " + contractAddress);
        }
    }

    public static class ContractAlreadyExistsException extends EVMException {
        public ContractAlreadyExistsException(String s) {
            super("Contract already exists: " + s);
        }
    }

    public static class ValueTransferException extends EVMException {
        public ValueTransferException(String msg, String error) {
            super("Value transfer failed: " + msg + ", error: " + error);
        }
    }

    public static class UnknownSystemException extends EVMException {
        public UnknownSystemException(Exception e) {
            super(e.getMessage());
        }
    }

    public static class ErrInsufficientBalance extends EVMException {
        public ErrInsufficientBalance(Address from) {
            super("Insufficient balance for address: " + from);
        }
    }

    public static class ErrMaxCodeSizeExceeded extends EVMException {
        public ErrMaxCodeSizeExceeded(int length) {
            super("Maximum code size exceeded: " + length + " bytes");
        }
    }

    public static class ErrCodeStoreOutOfGas extends EVMException {
        public ErrCodeStoreOutOfGas() {
            super("Out of gas while storing code");
        }
    }

    public static class ErrExecutionReverted extends EVMException {
        public ErrExecutionReverted(String reason) {
            super("Execution reverted: " + reason);
        }
    }
}
