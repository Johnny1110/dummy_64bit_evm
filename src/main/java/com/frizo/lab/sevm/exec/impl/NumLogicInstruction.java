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
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() < context.getCurrentStack().safePop() ? 1L : 0);
                break;
            case GT:
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() > context.getCurrentStack().safePop() ? 1L : 0);
                break;
            case SLT:
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() <= context.getCurrentStack().safePop() ? 1L : 0);
                break;
            case SGT:
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() >= context.getCurrentStack().safePop() ? 1L : 0);
                break;
            case EQ:
                context.getCurrentStack().safePush(Objects.equals(context.getCurrentStack().safePop(), context.getCurrentStack().safePop()) ? 1L : 0);
                break;
            case ISZERO:
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() == 0 ? 1L : 0);
                break;
            case AND:
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() & context.getCurrentStack().safePop());
                break;
            case OR:
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() | context.getCurrentStack().safePop());
                break;
            case XOR:
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() ^ context.getCurrentStack().safePop());
                break;
            case SHL:
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() << context.getCurrentStack().safePop());
                break;
            case SHR:
                context.getCurrentStack().safePush(context.getCurrentStack().safePop() >> context.getCurrentStack().safePop());
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
