package com.gerefi;

import com.devexperts.logging.Logging;
import com.opensr5.ConfigurationImage;
import com.opensr5.ini.field.ScalarIniField;
import com.gerefi.binaryprotocol.BinaryProtocol;
import com.gerefi.config.generated.Fields;
import com.gerefi.core.gerefiVersion;
import com.gerefi.io.ConnectionStateListener;
import com.gerefi.io.LinkManager;
import com.gerefi.io.tcp.BinaryProtocolServer;
import com.gerefi.io.tcp.TcpConnector;
import com.gerefi.io.tcp.TcpIoStream;
import com.gerefi.proxy.NetworkConnector;
import com.gerefi.proxy.NetworkConnectorContext;
import com.gerefi.proxy.client.LocalApplicationProxy;
import com.gerefi.proxy.client.LocalApplicationProxyContext;
import com.gerefi.proxy.client.UpdateType;
import com.gerefi.server.*;
import com.gerefi.tools.online.HttpUtil;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static com.devexperts.logging.Logging.getLogging;
import static com.gerefi.TestHelper.*;
import static com.gerefi.Timeouts.SECOND;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FullServerTest {
    private static final Logging log = getLogging(FullServerTest.class);

    @BeforeEach
    public void setup() throws MalformedURLException {
        BackendTestHelper.commonServerTest();
    }

    @Test
    public void testRelayWorkflow() throws InterruptedException, IOException {
        ScalarIniField iniField = TestHelper.createIniField(Fields.CYLINDERSCOUNT);
        int value = 241;
        int userId = 7;


        LocalApplicationProxyContext localApplicationProxyContext = new LocalApplicationProxyContext() {
            @Override
            public String executeGet(String url) throws IOException {
                return HttpUtil.executeGet(url);
            }

            @Override
            public int serverPortForRemoteApplications() {
                return 7003;
            }

            @Override
            public int authenticatorPort() {
                return 7004;
            }
        };

        CountDownLatch controllerRegistered = new CountDownLatch(1);
        CountDownLatch applicationClosed = new CountDownLatch(1);

        UserDetailsResolver userDetailsResolver = authToken -> new UserDetails(authToken.substring(0, 5), userId);
        int httpPort = 8103;
        int applicationTimeout = 7 * SECOND;
        try (Backend backend = new Backend(userDetailsResolver, httpPort, applicationTimeout) {
            @Override
            public void register(ControllerConnectionState controllerConnectionState) {
                super.register(controllerConnectionState);
                controllerRegistered.countDown();
            }

            @Override
            protected void close(ApplicationConnectionState applicationConnectionState) {
                super.close(applicationConnectionState);
                applicationClosed.countDown();
            }
        }; LinkManager clientManager = new LinkManager().setNeedPullData(false);
             NetworkConnector networkConnector = new NetworkConnector()) {
            int serverPortForControllers = 7001;


            // first start backend server
            BackendTestHelper.runControllerConnectorBlocking(backend, serverPortForControllers);
            BackendTestHelper.runApplicationConnectorBlocking(backend, localApplicationProxyContext.serverPortForRemoteApplications());

            // create virtual controller to which "gerefi network connector" connects to
            int controllerPort = 7002;
            ConfigurationImage controllerImage = prepareImage(value, createIniField(Fields.CYLINDERSCOUNT));
            TestHelper.createVirtualController(controllerPort, controllerImage, new BinaryProtocolServer.Context());

            CountDownLatch softwareUpdateRequest = new CountDownLatch(1);

            NetworkConnectorContext networkConnectorContext = new NetworkConnectorContext() {
                @Override
                public int serverPortForControllers() {
                    return serverPortForControllers;
                }

                @Override
                public void onConnectorSoftwareUpdateToLatestRequest() {
                    softwareUpdateRequest.countDown();
                }
            };

            // start "gerefi network connector" to connect controller with backend since in real life controller has only local serial port it does not have network
            NetworkConnector.NetworkConnectorResult networkConnectorResult = networkConnector.start(NetworkConnector.Implementation.Unknown,
                TestHelper.TEST_TOKEN_1, TcpConnector.LOCALHOST + ":" + controllerPort, networkConnectorContext, NetworkConnector.ReconnectListener.VOID);
            ControllerInfo controllerInfo = networkConnectorResult.getControllerInfo();

            TestHelper.assertLatch("controllerRegistered. todo: this test should not depend on internet connection and having real .ini on gerefi online", controllerRegistered);

            SessionDetails authenticatorSessionDetails = new SessionDetails(NetworkConnector.Implementation.Unknown, controllerInfo, TEST_TOKEN_3, networkConnectorResult.getOneTimeToken(), gerefiVersion.CONSOLE_VERSION);
            ApplicationRequest applicationRequest = new ApplicationRequest(authenticatorSessionDetails, userDetailsResolver.apply(TestHelper.TEST_TOKEN_1));

            HttpResponse response = LocalApplicationProxy.requestSoftwareUpdate(httpPort, applicationRequest, UpdateType.CONTROLLER);
            log.info("requestSoftwareUpdate response: " + response.toString());
            assertLatch("update requested", softwareUpdateRequest);

            // start authenticator
            LocalApplicationProxy.startAndRun(localApplicationProxyContext, applicationRequest, httpPort,
                TcpIoStream.DisconnectListener.VOID,
                LocalApplicationProxy.ConnectionListener.VOID);


            CountDownLatch connectionEstablishedCountDownLatch = new CountDownLatch(1);

            // connect to proxy and read virtual controller through it
            clientManager.startAndConnect(TcpConnector.LOCALHOST + ":" + localApplicationProxyContext.authenticatorPort(), new ConnectionStateListener() {
                @Override
                public void onConnectionEstablished() {
                    connectionEstablishedCountDownLatch.countDown();
                }

                @Override
                public void onConnectionFailed(String s) {
                    System.out.println("Failed");
                }
            });
            assertLatch("Proxied ECU Connection established", connectionEstablishedCountDownLatch);

            BinaryProtocol clientStreamState = clientManager.getCurrentStreamState();
            Objects.requireNonNull(clientStreamState, "clientStreamState");
            ConfigurationImage clientImage = clientStreamState.getControllerConfiguration();
            String clientValue = iniField.getValue(clientImage);
            assertEquals(Double.toString(value), clientValue);

            assertEquals(1, backend.getApplications().size());
            assertEquals(1, applicationClosed.getCount());

            // now let's test that application connector would be terminated by server due to inactivity
            log.info("**************************************");
            log.info("Sleeping twice the application timeout");
            log.info("**************************************");
            assertLatch("applicationClosed", applicationClosed, 3 * applicationTimeout);

            assertEquals(0, backend.getApplications().size(), "applications size");
        }
    }
}
