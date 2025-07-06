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
            case STOP -> context.stop();
            case ADD -> binaryOp(context, Integer::sum);
            case MUL -> binaryOp(context, (a, b) -> a * b);
            case SUB -> binaryOp(context, (a, b) -> a - b);
            case DIV -> binaryOp(context, (a, b) -> a / b);
            case ISZERO -> {
                // ISZERO: Pops the top value from the stack, pushes 1 if it is zero, otherwise pushes 0
                // Equals to NOT operation in some contexts
                if (context.getStack().isEmpty()) {
                    throw new EVMException.StackUnderflowException();
                }
                int value = context.getStack().safePop();
                context.getStack().safePush(value == 0 ? 1 : 0);
            }
            default -> throw new EVMException.UnknownOpcodeException(opcode);
        }
    }

    private void binaryOp(EVMContext context, BinaryOperator<Integer> op) {
        if (context.getStack().size() < 2) {
            throw new EVMException.StackUnderflowException();
        }
        int b = context.getStack().safePop();
        int a = context.getStack().safePop();
        int result = op.apply(a, b);
        context.getStack().safePush(result);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return switch (opcode) {
            case STOP, ADD, MUL, SUB, DIV -> true;
            default -> false;
        };
    }
}
