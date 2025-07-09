package com.frizo.lab.sevm.exec.impl;

import com.frizo.lab.sevm.context.EVMContext;
import com.frizo.lab.sevm.exception.EVMException;
import com.frizo.lab.sevm.exec.InstructionExecutor;
import com.frizo.lab.sevm.op.Opcode;
import com.frizo.lab.sevm.utils.NumUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvironmentalExecutor implements InstructionExecutor {

    @Override
    public void execute(EVMContext context, Opcode opcode) {
        log.info("[EnvironmentalExecutor] Executing opcode: {}", opcode);
        switch (opcode) {
            case ADDRESS:
                processAddress(context);
                break;
            case BALANCE:
                processBalance(context);
                break;
            case CALLER:
                processCaller(context);
                break;
            case CALLVALUE:
                processCallValue(context);
                break;
            default:
                log.error("Unsupported opcode: {}", opcode);
                throw new UnsupportedOperationException("Unsupported opcode: " + opcode);
        }
    }

    private void processAddress(EVMContext context) {
        String contractAddress = context.getCurrentFrame().getContractAddress();
        byte[] addressBytes = NumUtils.hexStringToBytes(contractAddress);
        long addressValue = NumUtils.bytesToLong(addressBytes);
        context.getCurrentStack().safePush(addressValue);
        log.info("[EnvironmentalExecutor] ADDRESS: convert address[{}] to value[{}] and push into stack.", contractAddress, addressValue);
    }

    private void processBalance(EVMContext context) {
        // pop 1 value from stack (address)
        if (context.getCurrentStack().isEmpty()) {
            throw new EVMException.StackUnderflowException("Not enough values on stack to process BALANCE");
        }
        long addressValue = context.getCurrentStack().safePop();
        String hexAddress = NumUtils.longToPaddingLeftHex(addressValue, 8);
        // get balance(ETH) of the address (unit wei)
        long balance = context.getBlockchain().balance(hexAddress);
        // push the balance value into stack
        context.getCurrentStack().safePush(balance);
    }

    private void processCaller(EVMContext context) {
        String callerAddress = context.getCurrentFrame().getCaller();
        byte[] addressBytes = NumUtils.hexStringToBytes(callerAddress);
        long addressValue = NumUtils.bytesToLong(addressBytes);
        context.getCurrentStack().safePush(addressValue);
        log.info("[EnvironmentalExecutor] CALLER: convert caller address[{}] to value[{}] and push into stack.", callerAddress, addressValue);

    }

    // Get the value of the call (if any) and push it onto the stack
    private void processCallValue(EVMContext context) {
        long callValue = context.getCurrentFrame().getValue();
        // unit: wei
        context.getCurrentStack().safePush(callValue);
    }

    @Override
    public boolean canHandle(Opcode opcode) {
        return opcode.getExecutorClass().equals(EnvironmentalExecutor.class);
    }
}
