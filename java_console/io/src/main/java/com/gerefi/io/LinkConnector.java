package com.gerefi.io;

import com.gerefi.binaryprotocol.BinaryProtocol;
import com.gerefi.binaryprotocol.BinaryProtocolState;
import org.jetbrains.annotations.NotNull;

/**
 * @author Andrey Belomutskiy
 *         3/3/14
 */
public interface LinkConnector extends LinkDecoder {
    LinkConnector VOID = new LinkConnector() {
        @Override
        public void connectAndReadConfiguration(BinaryProtocol.Arguments arguments, ConnectionStateListener listener) {
        }

        @Override
        public void send(String command, boolean fireEvent) {
        }

        @Override
        public BinaryProtocol getBinaryProtocol() {
            return null;
        }
    };

    @NotNull
    static LinkConnector getDetachedConnector(BinaryProtocolState state) {
        return new LinkConnector() {
            @Override
            public BinaryProtocolState getBinaryProtocolState() {
                return state;
            }

            @Override
            public void connectAndReadConfiguration(BinaryProtocol.Arguments arguments, ConnectionStateListener listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void send(String command, boolean fireEvent) {
                throw new UnsupportedOperationException();
            }

            @Override
            public BinaryProtocol getBinaryProtocol() {
                throw new UnsupportedOperationException();
            }
        };
    }

    void connectAndReadConfiguration(BinaryProtocol.Arguments arguments, ConnectionStateListener listener);

    void send(String command, boolean fireEvent) throws InterruptedException;

    BinaryProtocol getBinaryProtocol();

    default BinaryProtocolState getBinaryProtocolState() {
        return getBinaryProtocol().getBinaryProtocolState();
    }

    default void stop() {
    }
}
