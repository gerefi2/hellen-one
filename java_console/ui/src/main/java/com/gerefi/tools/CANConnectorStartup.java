package com.gerefi.tools;

import com.gerefi.binaryprotocol.BinaryProtocol;
import com.gerefi.io.serial.AbstractIoStream;
import com.gerefi.io.tcp.BinaryProtocolProxy;
import com.gerefi.io.tcp.TcpConnector;
import com.gerefi.ui.StatusConsumer;

import java.io.IOException;

public class CANConnectorStartup {
    public static void start(AbstractIoStream tsStream, StatusConsumer statusListener) throws IOException {
        if (tsStream == null)
            throw new IOException("Failed to initialise connector");

        String signature = BinaryProtocol.getSignature(tsStream);
        if (signature == null) {
            statusListener.append("Error: no ECU signature from " + tsStream);
        } else {
            statusListener.append("Got [" + signature + "] ECU signature via " + tsStream);
        }
        BinaryProtocolProxy.createProxy(tsStream, TcpConnector.DEFAULT_PORT, BinaryProtocolProxy.ClientApplicationActivityListener.VOID, statusListener);

    }
}
