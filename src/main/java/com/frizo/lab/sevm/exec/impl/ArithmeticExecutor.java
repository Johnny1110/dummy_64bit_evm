package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;

import java.util.function.BinaryOperator;

public class ArithmeticExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        switch (opcode) {
            case ADD -> binaryOp(context, Long::sum);
            case MUL -> binaryOp(context, (a, b) -> a * b);
            case SUB -> binaryOp(context, (a, b) -> a - b);
            case DIV -> binaryOp(context, (a, b) -> a / b);
            default -> throw new EVMException.UnknownOpcodeException(opcode);
        }
    }

    private void binaryOp(EVMContext context, BinaryOperator<Long> op) {
        if (context.getCurrentStack().size() < 2) {
            throw new EVMException.StackUnderflowException();
        }
        long b = context.getCurrentStack().safePop();
        long a = context.getCurrentStack().safePop();
        long result = op.apply(a, b);
        context.getCurrentStack().safePush(result);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return switch (opcode) {
            case ADD, MUL, SUB, DIV -> true;
            default -> false;
        };
    }
}
