package com.gerefi.io.commands;

import com.gerefi.binaryprotocol.IncomingDataBuffer;
import com.gerefi.config.generated.Fields;
import com.gerefi.io.IoStream;
import com.gerefi.io.tcp.BinaryProtocolServer;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;

import static com.gerefi.binaryprotocol.IoHelper.checkResponseCode;

public class HelloCommand implements Command {
    private final String tsSignature;

    public HelloCommand(String tsSignature) {
        this.tsSignature = tsSignature;
    }

    public static void send(IoStream stream) throws IOException {
        stream.sendPacket(new byte[]{Fields.TS_HELLO_COMMAND});
    }

    @Nullable
    public static String getHelloResponse(IncomingDataBuffer incomingData) throws EOFException {
        return getStringResponse("[hello]", incomingData);
    }

    @Nullable
    public static String getStringResponse(String msg, IncomingDataBuffer incomingData) throws EOFException {
        byte[] response = incomingData.getPacket(msg);
        if (!checkResponseCode(response))
            return null;
        return new String(response, 1, response.length - 1);
    }

    @Override
    public byte getCommand() {
        return Fields.TS_HELLO_COMMAND;
    }

    @Override
    public void handle(IoStream stream) throws IOException {
        stream.sendPacket((BinaryProtocolServer.TS_OK + tsSignature).getBytes());
    }
}
