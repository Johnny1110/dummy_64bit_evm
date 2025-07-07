package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class NumLogicInstruction implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[NumLogicInstruction] Executing Opcode: {}", opcode);
        switch (opcode) {
            case LT:
                context.getStack().safePush(context.getStack().safePop() < context.getStack().safePop() ? 1L : 0);
                break;
            case GT:
                context.getStack().safePush(context.getStack().safePop() > context.getStack().safePop() ? 1L : 0);
                break;
            case SLT:
                context.getStack().safePush(context.getStack().safePop() <= context.getStack().safePop() ? 1L : 0);
                break;
            case SGT:
                context.getStack().safePush(context.getStack().safePop() >= context.getStack().safePop() ? 1L : 0);
                break;
            case EQ:
                context.getStack().safePush(Objects.equals(context.getStack().safePop(), context.getStack().safePop()) ? 1L : 0);
                break;
            case ISZERO:
                context.getStack().safePush(context.getStack().safePop() == 0 ? 1L : 0);
                break;
            case AND:
                context.getStack().safePush(context.getStack().safePop() & context.getStack().safePop());
                break;
            case OR:
                context.getStack().safePush(context.getStack().safePop() | context.getStack().safePop());
                break;
            case XOR:
                context.getStack().safePush(context.getStack().safePop() ^ context.getStack().safePop());
                break;
            case SHL:
                context.getStack().safePush(context.getStack().safePop() << context.getStack().safePop());
                break;
            case SHR:
                context.getStack().safePush(context.getStack().safePop() >> context.getStack().safePop());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
        }
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.getExecutorClass().equals(NumLogicInstruction.class);
    }
}
