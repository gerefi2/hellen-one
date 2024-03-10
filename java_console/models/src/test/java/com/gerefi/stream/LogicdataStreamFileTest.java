package com.gerefi.stream;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogicdataStreamFileTest {
    @Test
    public void testWriteAs() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LogicdataOutputStream logicdataOutputStream = new LogicdataOutputStream(baos);
        logicdataOutputStream.writeVarLength(10);

        byte[] bytes = baos.toByteArray();
        assertEquals(2, bytes.length);
        assertEquals(1, bytes[0]);
        assertEquals(10, bytes[1]);
    }
}
