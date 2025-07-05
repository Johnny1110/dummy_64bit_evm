package com.frizo.lab.sevm.exception;

import com.frizo.lab.sevm.nums.Opcode;

public class EVMException extends RuntimeException {
    public EVMException(String message) {
        super(message);
    }

    public static class StackUnderflowException extends EVMException {
        public StackUnderflowException() {
            super("Stack underflow: not enough items on the stack");
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
}
