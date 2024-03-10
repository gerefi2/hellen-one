package com.gerefi.binaryprotocol.test;

import com.gerefi.binaryprotocol.IoHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IoHelperTest {
    @Test
    public void test255() {
        assertEquals(255, IoHelper.getInt((byte) 0, (byte) -1));
    }

    @Test
    public void testSwap16() throws IOException {
        byte[] packet = {-12, 0};
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet));
        short a = dis.readShort();
        int x = IoHelper.swap16(a);
        assertEquals(244, x);
    }
}
