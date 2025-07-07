package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.context.call.CallReturnDataBuffer;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.utils.MemoryUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReturnDataExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[ReturnDataExecutor] Executing opcode: {}", opcode);

        CallReturnDataBuffer buffer = context.getCurrentFrame().getCallReturnBuffer();
        switch (opcode) {
            case RETURNDATASIZE:
                // Push the size of the return data onto the stack
                context.getStack().safePush(buffer.getReturnSize());
                break;

            case RETURNDATACOPY:
                // structure of the stack: [destOffset, offset, size]
                if (context.getStack().size() < 3) {
                    throw new EVMException.StackUnderflowException("Not enough items on the stack for RETURNDATACOPY");
                }

                // Copy return data to memory
                long memoryOffset = context.getStack().safePop();     // destOffset
                long returnDataOffset = context.getStack().safePop(); // offset
                long length = context.getStack().safePop();           // size
                if (buffer.getReturnSize() == 0) {
                    return;
                }

                if (returnDataOffset + length > buffer.getReturnSize()) {
                    throw new EVMException.InvalidMemoryAccess("Return data out of bounds");
                }

                // Copy specific length of return data
                byte[] dataToCopy = buffer.getReturnData(returnDataOffset, length);
                MemoryUtils.write(context, memoryOffset, dataToCopy);

                break;

            default:
                throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
        }
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.getExecutorClass().equals(ReturnDataExecutor.class);
    }
}
