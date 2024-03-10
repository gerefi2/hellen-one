package com.gerefi.io;

import com.opensr5.ConfigurationImage;
import com.opensr5.ini.field.ScalarIniField;
import com.gerefi.TestHelper;
import com.gerefi.binaryprotocol.BinaryProtocol;
import com.gerefi.config.generated.Fields;
import com.gerefi.io.tcp.BinaryProtocolProxy;
import com.gerefi.io.tcp.BinaryProtocolServer;
import com.gerefi.io.tcp.TcpConnector;
import com.gerefi.ui.StatusConsumer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gerefi.TestHelper.assertLatch;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TcpCommunicationIntegrationTest {
    // todo: implement & test TCP connector restart!
    @Test
    public void testConnectionFailed() throws InterruptedException {
        int port = 6101;

        CountDownLatch failedCountDownLatch = new CountDownLatch(1);

        LinkManager clientManager = new LinkManager();
        clientManager.startAndConnect(Integer.toString(port), new ConnectionStateListener() {
            @Override
            public void onConnectionEstablished() {
                System.out.println("Established");
            }

            @Override
            public void onConnectionFailed(String s) {
                System.out.println("onConnectionFailed");
                failedCountDownLatch.countDown();
            }
        });

        assertLatch(failedCountDownLatch);
    }

    @Test
    public void testConnectAndTransmitImageOverTcpIp() throws InterruptedException {
        ScalarIniField iniField = TestHelper.createIniField(Fields.CYLINDERSCOUNT);
        int value = 239;
        ConfigurationImage serverImage = TestHelper.prepareImage(value, iniField);
        int port = 6100;

        BinaryProtocolServer server = TestHelper.createVirtualController(port, serverImage, new BinaryProtocolServer.Context());

        CountDownLatch connectionEstablishedCountDownLatch = new CountDownLatch(1);

        // todo: remove CONFIGURATION_gerefi_BINARY or nicer API to disable local file load

        LinkManager clientManager = new LinkManager();
        clientManager.startAndConnect(TcpConnector.LOCALHOST + ":" + port, new ConnectionStateListener() {
            @Override
            public void onConnectionEstablished() {
                connectionEstablishedCountDownLatch.countDown();
            }

            @Override
            public void onConnectionFailed(String s) {
                System.out.println("Failed");
            }
        });
        assertLatch("Connection established", connectionEstablishedCountDownLatch);

        assertEquals(0, server.unknownCommands.get());

        BinaryProtocol clientStreamState = clientManager.getCurrentStreamState();
        Objects.requireNonNull(clientStreamState, "clientStreamState");
        ConfigurationImage clientImage = clientStreamState.getControllerConfiguration();
        String clientValue = iniField.getValue(clientImage);
        assertEquals(Double.toString(value), clientValue);

        clientManager.close();
    }

    @Test
    public void testProxy() throws InterruptedException, IOException {
        ConfigurationImage serverImage = TestHelper.prepareImage(239, TestHelper.createIniField(Fields.CYLINDERSCOUNT));
        int controllerPort = 6102;

        // create virtual controller
        TestHelper.createVirtualController(controllerPort, serverImage, new BinaryProtocolServer.Context());
        int proxyPort = 6103;


        // connect proxy to virtual controller
        IoStream targetEcuSocket = TestHelper.connectToLocalhost(controllerPort);
        final AtomicInteger relayCommandCounter = new AtomicInteger();
        BinaryProtocolProxy.createProxy(targetEcuSocket, proxyPort, () -> relayCommandCounter.incrementAndGet(),
                StatusConsumer.ANONYMOUS);

        CountDownLatch connectionEstablishedCountDownLatch = new CountDownLatch(1);

        // connect to proxy and read virtual controller through it
        LinkManager clientManager = new LinkManager();
        clientManager.startAndConnect(TcpConnector.LOCALHOST + ":" + proxyPort, new ConnectionStateListener() {
            @Override
            public void onConnectionEstablished() {
                connectionEstablishedCountDownLatch.countDown();
            }

            @Override
            public void onConnectionFailed(String s) {
                System.out.println("Failed");
            }
        });
        assertLatch("Connection established", connectionEstablishedCountDownLatch);

        clientManager.close();
    }

}
