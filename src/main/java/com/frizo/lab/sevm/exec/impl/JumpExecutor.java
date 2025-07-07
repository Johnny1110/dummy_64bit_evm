package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JumpExecutor implements InstructionExecutor {
    @Override
    public void execute(EVMContext context, Opcode opcode) {
        switch (opcode) {
            case JUMPDEST -> {
                log.info("[JumpExecutor] <JUMPDEST> located, pc: {}", context.getPc());
                // JUMPDEST does not affect stack or pc, just marks a valid jump destination
                // No action needed here, just for validation
            }

            case JUMP -> {
                int destIdx = Math.toIntExact(context.getStack().safePop());
                requiredValidJump(context, destIdx);
                Opcode targetOp = Opcode.fromByte(context.getCode()[destIdx]);
                log.info("[JumpExecutor] <JUMP> to destination index: {}, tartget:{}", destIdx, targetOp);
                context.updatePC(destIdx); // set pc to the destination index
            }
            case JUMPI -> {
                int dest = Math.toIntExact(context.getStack().safePop());
                long condition = context.getStack().safePop();
                if (condition != 0) {
                    requiredValidJump(context, dest);
                    Opcode targetOp = Opcode.fromByte(context.getCode()[dest]);
                    log.info("[JumpExecutor] <JUMPI> to destination index: {}, target: {}", dest, targetOp);
                    context.updatePC(dest); // set pc to the destination index if condition is true
                }
            }
        }
    }

    private void requiredValidJump(EVMContext context, int destIdx) {
        if (!context.getValidJumpDestIdx().contains(destIdx)) {
            throw new EVMException.InvalidJumpException();
        }
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return switch (opcode) {
            case JUMP, JUMPI, JUMPDEST -> true;
            default -> false;
        };
    }
}
