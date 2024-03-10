package com.gerefi.io.commands;

import com.gerefi.config.generated.Fields;
import com.gerefi.io.IoStream;
import com.gerefi.io.tcp.BinaryProtocolServer;

import java.io.IOException;

public class GetOutputsCommand {

    public static byte[] createRequest() {
        return createRequest(0, Fields.TS_TOTAL_OUTPUT_SIZE);
    }

    public static byte[] createRequest(int offset, int size) {
        byte[] packet = new byte[4];
        ByteRange.packOffsetAndSize(offset, size, packet);
        return packet;
    }

    public static void sendOutput(IoStream stream) throws IOException {
        byte[] response = new byte[1 + Fields.TS_TOTAL_OUTPUT_SIZE];
        response[0] = (byte) BinaryProtocolServer.TS_OK.charAt(0);
        stream.sendPacket(response);
    }
}
