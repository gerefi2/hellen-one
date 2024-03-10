package com.gerefi.proxy;

import com.devexperts.logging.Logging;
import com.opensr5.ConfigurationImage;
import com.gerefi.Timeouts;
import com.gerefi.binaryprotocol.BinaryProtocol;
import com.gerefi.config.generated.Fields;
import com.gerefi.io.AbstractConnectionStateListener;
import com.gerefi.io.IoStream;
import com.gerefi.io.LinkManager;
import com.gerefi.io.commands.HelloCommand;
import com.gerefi.io.tcp.BinaryProtocolServer;
import com.gerefi.io.tcp.TcpIoStream;
import com.gerefi.core.gerefiVersion;
import com.gerefi.server.ControllerInfo;
import com.gerefi.server.SessionDetails;
import com.gerefi.server.gerefiSSLContext;
import com.gerefi.tools.VehicleToken;
import com.gerefi.tools.online.HttpUtil;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.gerefi.binaryprotocol.BinaryProtocol.sleep;

/**
 * Connector between gerefi ECU and gerefi server
 * see NetworkConnectorStartup
 */
public class NetworkConnector implements Closeable {
    /**
     * @see NetworkConnectorContext
     * See broadcash.sh
     */
    public static final byte DISCONNECT = 14;
    public static final byte UPDATE_CONNECTOR_SOFTWARE_LATEST = 15;
    public static final byte UPDATE_FIRMWARE_LATEST = 16;
    public static final byte UPDATE_CONNECTOR_SOFTWARE_RELEASE = 17;
    public static final byte UPDATE_FIRMWARE_RELEASE = 17;
    private final static Logging log = Logging.getLogging(NetworkConnector.class);
    private boolean isClosed;

    public NetworkConnectorResult start(Implementation implementation, String authToken, String controllerPort, NetworkConnectorContext context) {
        return start(implementation, authToken, controllerPort, context, new ReconnectListener() {
            @Override
            public void onReconnect() {
                log.info("onReconnect");
            }
        });
    }

    public NetworkConnectorResult start(Implementation implementation, String authToken, String controllerPort, NetworkConnectorContext context, ReconnectListener reconnectListener) {
        LinkManager controllerConnector = new LinkManager()
                .setCompositeLogicEnabled(false)
                .setNeedPullData(false);

        CountDownLatch onConnected = new CountDownLatch(1);
        controllerConnector.startAndConnect(controllerPort, new AbstractConnectionStateListener() {
            @Override
            public void onConnectionEstablished() {
                onConnected.countDown();
            }
        });

        log.info("Connecting to controller...");
        try {
            onConnected.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        if (onConnected.getCount() != 0) {
            log.info("Connection to controller failed");
            return NetworkConnectorResult.ERROR;
        }

        return start(implementation, authToken, context, reconnectListener, controllerConnector, ActivityListener.VOID);
    }

    public NetworkConnectorResult start(Implementation implementation, String authToken, NetworkConnectorContext context, ReconnectListener reconnectListener, LinkManager linkManager, ActivityListener activityListener) {
        ControllerInfo controllerInfo;
        try {
            controllerInfo = getControllerInfo(linkManager, linkManager.getConnector().getBinaryProtocol().getStream());
        } catch (IOException e) {
            return NetworkConnectorResult.ERROR;
        }

        int vehicleToken = VehicleToken.getOrCreate();

        BinaryProtocolServer.getThreadFactory("Proxy Reconnect").newThread(() -> {
            Semaphore proxyReconnectSemaphore = new Semaphore(1);
            try {
                while (!isClosed) {
                    proxyReconnectSemaphore.acquire();

                    try {
                        start(implementation,
                                activityListener,
                                context.serverPortForControllers(), linkManager, authToken, (String message) -> {
                                    log.error(message + " Disconnect from proxy server detected, now sleeping " + context.reconnectDelay() + " seconds");
                                    sleep(context.reconnectDelay() * Timeouts.SECOND);
                                    log.debug("Releasing semaphore");
                                    proxyReconnectSemaphore.release();
                                    reconnectListener.onReconnect();
                                }, vehicleToken, controllerInfo, context);
                    } catch (IOException e) {
                        log.error("IO error", e);
                    }
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }).start();

        return new NetworkConnectorResult(controllerInfo, vehicleToken);
    }

    @NotNull
    private static SessionDetails start(Implementation implementation, ActivityListener activityListener, int serverPortForControllers, LinkManager linkManager, String authToken, final TcpIoStream.DisconnectListener disconnectListener, int oneTimeToken, ControllerInfo controllerInfo, final NetworkConnectorContext context) throws IOException {
        IoStream targetEcuSocket = linkManager.getConnector().getBinaryProtocol().getStream();

        SessionDetails deviceSessionDetails = new SessionDetails(implementation, controllerInfo, authToken, oneTimeToken, gerefiVersion.CONSOLE_VERSION);

        Socket socket;
        try {
            log.info("Connecting to proxy server " + HttpUtil.gerefi_PROXY_HOSTNAME + " " + serverPortForControllers);
            socket = gerefiSSLContext.getSSLSocket(HttpUtil.gerefi_PROXY_HOSTNAME, serverPortForControllers);
        } catch (IOException e) {
            // socket open exception is a special case and should be handled separately
            disconnectListener.onDisconnect("on socket open");
            return deviceSessionDetails;
        }
        BaseBroadcastingThread baseBroadcastingThread = new BaseBroadcastingThread(socket,
                deviceSessionDetails,
                disconnectListener, context) {
            @Override
            protected void handleCommand(BinaryProtocolServer.Packet packet, TcpIoStream stream) throws IOException {
                super.handleCommand(packet, stream);
                byte command = packet.getPacket()[0];
                if (command == Fields.TS_ONLINE_PROTOCOL) {
                    byte connectorCommand = packet.getPacket()[1];
                    log.info("Got connector command " + packet.getPacket());
                    if (connectorCommand == NetworkConnector.UPDATE_CONNECTOR_SOFTWARE_LATEST) {
                        context.onConnectorSoftwareUpdateToLatestRequest();
                    } else if (connectorCommand == NetworkConnector.UPDATE_CONNECTOR_SOFTWARE_RELEASE) {
                        context.onConnectorSoftwareUpdateToReleaseRequest();
                    } else if (connectorCommand == NetworkConnector.UPDATE_FIRMWARE_LATEST) {
                        context.onFirmwareUpdateToLatestRequest();
                    } else if (connectorCommand == NetworkConnector.UPDATE_FIRMWARE_RELEASE) {
                        context.onFirmwareUpdateToReleaseRequest();
                    }
                    return;
                }

                log.info("Relaying request to controller " + BinaryProtocol.findCommand(command));
                targetEcuSocket.sendPacket(packet);

                BinaryProtocolServer.Packet response = targetEcuSocket.readPacket();
                log.info("Relaying response to proxy size=" + response.getPacket().length);
                stream.sendPacket(response);
                activityListener.onActivity(targetEcuSocket);
            }
        };
        baseBroadcastingThread.start();
        return deviceSessionDetails;
    }

    @NotNull
    private static ControllerInfo getControllerInfo(LinkManager linkManager, IoStream targetEcuSocket) throws IOException {
        HelloCommand.send(targetEcuSocket);
        String helloResponse = HelloCommand.getHelloResponse(targetEcuSocket.getDataBuffer());
        if (helloResponse == null)
            throw new IOException("Error getting hello response");
        String controllerSignature = helloResponse.trim();

        ConfigurationImage image = linkManager.getConnector().getBinaryProtocol().getControllerConfiguration();
        String vehicleName = Fields.VEHICLENAME.getStringValue(image);
        String engineMake = Fields.ENGINEMAKE.getStringValue(image);
        String engineCode = Fields.ENGINECODE.getStringValue(image);
        return new ControllerInfo(vehicleName, engineMake, engineCode, controllerSignature);
    }

    @Override
    public void close() {
        isClosed = true;
    }

    public static class NetworkConnectorResult {
        static NetworkConnectorResult ERROR = new NetworkConnectorResult(null, 0);
        private final ControllerInfo controllerInfo;
        private final int oneTimeToken;

        public NetworkConnectorResult(ControllerInfo controllerInfo, int oneTimeToken) {
            this.controllerInfo = controllerInfo;
            this.oneTimeToken = oneTimeToken;
        }

        public ControllerInfo getControllerInfo() {
            return controllerInfo;
        }

        public int getOneTimeToken() {
            return oneTimeToken;
        }

        @Override
        public String toString() {
            return "NetworkConnectorResult{" +
                    "controllerInfo=" + controllerInfo +
                    '}';
        }
    }

    public interface ReconnectListener {
        ReconnectListener VOID = new ReconnectListener() {
            @Override
            public void onReconnect() {

            }
        };

        void onReconnect();
    }

    public interface ActivityListener {
        ActivityListener VOID = new ActivityListener() {
            @Override
            public void onActivity(IoStream targetEcuSocket) {

            }
        };
        void onActivity(IoStream targetEcuSocket);
    }

    public enum Implementation {
        Android,
        Plugin,
        SBC,
        Unknown;

        public static Implementation find(String name) {
            for (Implementation implementation : values()) {
                if (implementation.name().equalsIgnoreCase(name))
                    return implementation;
            }
            return Unknown;
        }
    }
}
