package com.frizo.lab.sevm.compiler;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SimpleEVMCompiler {

    private final List<Byte> bytecode = new ArrayList<>();

    public byte[] compile(String source) {
        // TODO: Implement a simple EVM compiler that compiles a source code string into bytecode.
        return toByteArray(bytecode);
    }

    private byte[] toByteArray(List<Byte> list) {
        byte[] arr = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
}
