package com.gerefi.io.can.isotp;

import com.devexperts.logging.Logging;
import com.gerefi.util.HexBinary;

import java.util.Arrays;

/**
 * ISO 15765-2 or ISO-TP (Transport Layer) CAN multi-frame decoder state
 * @see IsoTpConnector
 */
public class IsoTpCanDecoder {
    public static final byte[] FLOW_CONTROL = {0x30, 0, 0, 0, 0, 0, 0, 0};
    private static final Logging log = Logging.getLogging(IsoTpCanDecoder.class);

    static {
        log.configureDebugEnabled(false);
    }

    private final static int ISO_TP_FRAME_FLOW_CONTROL = 3;

    private final static int FC_ContinueToSend = 0;

    private int waitingForNumBytes = 0;
    private int waitingForFrameIndex = 0;

    public byte[] decodePacket(byte[] data) {
        int frameType = (data[0] >> 4) & 0xf;
        int numBytesAvailable;
        int frameIdx;
        int dataOffset;
        switch (frameType) {
            case IsoTpConstants.ISO_TP_FRAME_SINGLE:
                numBytesAvailable = data[0] & 0xf;
                dataOffset = 1;
                this.waitingForNumBytes = 0;
                if (log.debugEnabled())
                    log.debug("ISO_TP_FRAME_SINGLE " + numBytesAvailable);
                break;
            case IsoTpConstants.ISO_TP_FRAME_FIRST:
                this.waitingForNumBytes = ((data[0] & 0xf) << 8) | data[1];
                if (log.debugEnabled())
                    log.debug("Total expected: " + waitingForNumBytes);
                this.waitingForFrameIndex = 1;
                numBytesAvailable = Math.min(this.waitingForNumBytes, 6);
                waitingForNumBytes -= numBytesAvailable;
                dataOffset = 2;
                onTpFirstFrame();
                break;
            case IsoTpConstants.ISO_TP_FRAME_CONSECUTIVE:
                frameIdx = data[0] & 0xf;
                if (this.waitingForNumBytes < 0 || this.waitingForFrameIndex != frameIdx) {
                    throw new IllegalStateException("ISO_TP_FRAME_CONSECUTIVE: That's an abnormal situation, and we probably should react? waitingForNumBytes=" + waitingForNumBytes + " waitingForFrameIndex=" + waitingForFrameIndex + " frameIdx=" + frameIdx);
                }
                this.waitingForFrameIndex = (this.waitingForFrameIndex + 1) & 0xf;
                numBytesAvailable = Math.min(this.waitingForNumBytes, 7);
                dataOffset = 1;
                waitingForNumBytes -= numBytesAvailable;
                if (log.debugEnabled())
                    log.debug("ISO_TP_FRAME_CONSECUTIVE Got " + numBytesAvailable + " byte(s), still expecting: " + waitingForNumBytes + " byte(s)");
                break;
            case ISO_TP_FRAME_FLOW_CONTROL:
                int flowStatus = data[0] & 0xf;
                int blockSize = data[1];
                int separationTime = data[2];
                if (flowStatus == FC_ContinueToSend && blockSize == 0 && separationTime == 0)
                    return new byte[0];
                throw new IllegalStateException("ISO_TP_FRAME_FLOW_CONTROL: should we just ignore the FC frame? " + flowStatus + " " + blockSize + " " + separationTime);
            default:
                throw new IllegalStateException("Unknown frame type");
        }
        byte[] bytes = Arrays.copyOfRange(data, dataOffset, dataOffset + numBytesAvailable);
        if (log.debugEnabled())
            log.debug(numBytesAvailable + " bytes(s) arrived in this packet: " + HexBinary.printByteArray(bytes));
        return bytes;
    }

    protected void onTpFirstFrame() {
    }
}
