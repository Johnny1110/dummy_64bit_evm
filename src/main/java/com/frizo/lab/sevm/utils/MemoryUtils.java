package com.frizo.lab.sevm.utils;

import com.frizo.lab.sevm.context.EVMContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryUtils {

    public static byte[] read(EVMContext context, long offset, long size) {
        log.info("[MemoryUtils] Reading memory data from frame: [{}] offset: [{}], size: [{}]", context.getCurrentFrame().getFrameId(), offset, size);
        byte[] data = new byte[(int) size];
        for (int i = 0; i < size; i++) {
            byte memData = context.getCurrentMemory().get(offset + i);
            data[i] = memData;
        }
        return data;
    }

    public static void write(EVMContext context, long offset, byte[] data) {
        log.info("[MemoryUtils] Writing memory data to frame: [{}] offset: [{}]", context.getCurrentFrame().getFrameId(), offset);
        if (data == null || data.length == 0) {
            log.warn("[MemoryUtils] Attempted to write null or empty data to memory at offset: [{}]", offset);
            return;
        }
        if (offset < 0) {
            throw new IndexOutOfBoundsException("Memory write out of bounds: offset=" + offset + ", size=" + data.length);
        }
        for (int i = 0; i < data.length; i++) {
            context.getCurrentMemory().put(offset + i, data[i]);
        }
    }
}
