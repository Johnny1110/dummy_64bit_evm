package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.call.CallFrame;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.stack.call.CallStack;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallExecutor implements InstructionExecutor {
    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[CallExecutor] Executing: {}", opcode);

        switch (opcode) {
            case CALL:
                executeCall(context);
                break;
            case CALLCODE:
                executeCallCode(context);
                break;
            case DELEGATECALL:
                executeDelegateCall(context);
                break;
            case STATICCALL:
                executeStaticCall(context);
                break;
            case ICALL:
                executeInternalCall(context);
                break;
            default:
                throw new EVMException.UnknownOpcodeException(opcode);
        }
    }

    private void executeCall(EVMContext context) {
        Stack<Integer> stack = context.getStack();
        CallStack callStack = context.getCallStack();
        CallFrame currentFrame = callStack.();
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        // Check if the opcode is a CALL instruction
        return opcode.isCall();
    }
}
