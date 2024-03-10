package com.gerefi.io.can;

import com.devexperts.logging.Logging;
import com.opensr5.io.DataListener;
import com.gerefi.binaryprotocol.IncomingDataBuffer;
import com.gerefi.config.generated.Fields;
import com.gerefi.uds.CanConnector;
import com.gerefi.util.HexBinary;
import com.gerefi.io.IoStream;
import com.gerefi.io.can.isotp.IsoTpCanDecoder;
import com.gerefi.io.can.isotp.IsoTpConnector;
import com.gerefi.io.serial.AbstractIoStream;
import com.gerefi.io.tcp.BinaryProtocolServer;
import org.jetbrains.annotations.Nullable;
import tel.schich.javacan.RawCanChannel;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.devexperts.logging.Logging.getLogging;
import static com.gerefi.config.generated.Fields.CAN_ECU_SERIAL_TX_ID;

public class SocketCANIoStream extends AbstractIoStream {
    static Logging log = getLogging(SocketCANIoStream.class);
    private final IncomingDataBuffer dataBuffer;
    private final RawCanChannel socket;

    private final IsoTpCanDecoder canDecoder = new IsoTpCanDecoder() {
        @Override
        protected void onTpFirstFrame() {
            sendCanPacket(FLOW_CONTROL);
        }
    };

    private final IsoTpConnector isoTpConnector = new IsoTpConnector(Fields.CAN_ECU_SERIAL_RX_ID) {
        @Override
        public void sendCanData(byte[] total) {
            sendCanPacket(total);
        }
    };

    private void sendCanPacket(byte[] total) {
        if (log.debugEnabled())
            log.debug("-------sendIsoTp " + total.length + " byte(s):");

        if (log.debugEnabled())
            log.debug("Sending " + HexBinary.printHexBinary(total));

        SocketCANHelper.send(isoTpConnector.canId(), total, socket);
    }

    public SocketCANIoStream() {
        socket = SocketCANHelper.createSocket();
        // buffer could only be created once socket variable is not null due to callback
        dataBuffer = createDataBuffer();
    }

    @Nullable
    public static SocketCANIoStream create() {
        return new SocketCANIoStream();
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        IsoTpConnector.sendStrategy(bytes, isoTpConnector);
    }

    @Override
    public void setInputListener(DataListener listener) {
        Executor threadExecutor = Executors.newSingleThreadExecutor(BinaryProtocolServer.getThreadFactory("SocketCAN reader"));
        threadExecutor.execute(() -> {
            while (!isClosed()) {
                readOnePacket(listener);
            }
        });
    }

    private void readOnePacket(DataListener listener) {
        try {
            CanConnector.CanPacket rx = SocketCANHelper.read(socket);
            if (rx.id() != CAN_ECU_SERIAL_TX_ID) {
                if (log.debugEnabled())
                    log.debug("Skipping non " + String.format("%X", CAN_ECU_SERIAL_TX_ID) + " packet: " + String.format("%X", rx.id()));
                return;
            }
            byte[] decode = canDecoder.decodePacket(rx.payload());
            listener.onDataArrived(decode);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public IncomingDataBuffer getDataBuffer() {
        return dataBuffer;
    }

    public static IoStream createStream() {
        return new SocketCANIoStream();
    }
}
