package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.stack.Stack;
import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrintExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        // PRINT structure: pop order: 1. length, 2~x. print data. at least 2 values are required on the stack.
        Stack<Long> stack = context.getStack();
        if (stack.size() < 2) {
            throw new EVMException.StackUnderflowException(
                    "PRINT requires at least 2 values on the stack, but only " + stack.size() + " found.");
        }
        long length = stack.safePop();

        if (length < 0 || length > stack.size()) {
            throw new EVMException.InvalidStackOperationException(
                    "PRINT length must be between 0 and " + stack.size() + ", but got " + length);
        }

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < length; i++) {
            long value = stack.safePop();
            String valueStr = NumUtils.longToTrimmedString(value);
            output.append(valueStr);
        }

        System.out.println(output.toString().trim());
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return Opcode.PRINT.equals(opcode);
    }
}
