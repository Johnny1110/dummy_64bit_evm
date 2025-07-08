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
                long returnSize = buffer.getReturnSize();
                log.info("[ReturnDataExecutor] RETURNDATASIZE: the return size is [{}]", returnSize);
                context.getCurrentStack().safePush(returnSize);
                break;

            case RETURNDATACOPY:
                // structure of the stack: [destOffset, offset, size]
                if (context.getCurrentStack().size() < 3) {
                    throw new EVMException.StackUnderflowException("Not enough items on the stack for RETURNDATACOPY");
                }

                // Copy return data to memory
                long memoryOffset = context.getCurrentStack().safePop();     // destOffset
                long returnDataOffset = context.getCurrentStack().safePop(); // offset
                long length = context.getCurrentStack().safePop();           // size

                log.info("[ReturnDataExecutor] RETURNDATACOPY - memoryOffset: {}, returnDataOffset: {}, length: {}",
                        memoryOffset, returnDataOffset, length);

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
