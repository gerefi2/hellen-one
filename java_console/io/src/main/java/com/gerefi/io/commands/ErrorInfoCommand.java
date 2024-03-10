package com.gerefi.io.commands;

import com.gerefi.binaryprotocol.IncomingDataBuffer;
import com.gerefi.config.generated.Fields;
import com.gerefi.io.IoStream;

import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;

import static com.gerefi.io.commands.HelloCommand.getStringResponse;

public class ErrorInfoCommand {
    public static void send(IoStream stream) throws IOException {
        stream.sendPacket(new byte[]{Fields.TS_GET_CONFIG_ERROR});
    }

    @Nullable
    public static String getResponse(IncomingDataBuffer incomingData) throws EOFException {
        return getStringResponse("[config_error]", incomingData);
    }

}
