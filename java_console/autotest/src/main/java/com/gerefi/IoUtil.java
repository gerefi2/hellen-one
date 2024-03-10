package com.gerefi;

import com.devexperts.logging.Logging;
import com.gerefi.config.generated.Fields;
import com.gerefi.core.EngineState;
import com.gerefi.core.ISensorCentral;
import com.gerefi.core.Sensor;
import com.gerefi.core.SensorCentral;
import com.gerefi.enums.trigger_type_e;
import com.gerefi.io.CommandQueue;
import com.gerefi.io.ConnectionStateListener;
import com.gerefi.io.LinkManager;
import com.gerefi.io.tcp.TcpConnector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.devexperts.logging.Logging.getLogging;
import static com.gerefi.config.generated.Fields.CMD_RPM;
import static com.gerefi.waves.EngineReport.isCloseEnough;

/**
 * @author Andrey Belomutskiy
 *         3/19/14.
 */
public class IoUtil {
    private static final Logging log = getLogging(IoUtil.class);

    /**
     * Send a command and wait for the confirmation
     *
     * @throws IllegalStateException if command was not confirmed
     */
    static void sendBlockingCommand(String command, CommandQueue commandQueue) {
        sendBlockingCommand(command, CommandQueue.DEFAULT_TIMEOUT, commandQueue);
    }

    public static String getSetCommand(String settingName) {
        return Fields.CMD_SET + " " + settingName;
    }

    public static String getEnableCommand(String settingName) {
        return Fields.CMD_ENABLE + " " + settingName;
    }

    public static String getDisableCommand(String settingName) {
        return Fields.CMD_DISABLE + " " + settingName;
    }

    /**
     * blocking method which would for confirmation from gerefi
     */
    public static void sendBlockingCommand(String command, int timeoutMs, CommandQueue commandQueue) {
        final CountDownLatch responseLatch = new CountDownLatch(1);
        long time = System.currentTimeMillis();
        log.info("Sending command [" + command + "]");
        final long begin = System.currentTimeMillis();
        commandQueue.write(command, timeoutMs, () -> {
            responseLatch.countDown();
            log.info("Got confirmation in " + (System.currentTimeMillis() - begin) + "ms");
        });
        wait(responseLatch, timeoutMs);
        if (responseLatch.getCount() > 0)
            log.info("No confirmation in " + timeoutMs);
        log.info("Command [" + command + "] executed in " + (System.currentTimeMillis() - time));
    }

    static void wait(CountDownLatch responseLatch, int milliseconds) {
        try {
            responseLatch.await(milliseconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void changeRpm(CommandQueue commandQueue, final int rpm) {
        log.info("AUTOTEST rpm EN " + rpm);
        sendBlockingCommand(CMD_RPM + " " + rpm, commandQueue);
        long time = System.currentTimeMillis();

        awaitRpm(rpm);

        double actualRpm = SensorCentral.getInstance().getValue(Sensor.RPMValue);

        if (!isCloseEnough(rpm, actualRpm))
            throw new IllegalStateException("rpm change did not happen: " + rpm + ", actual " + actualRpm);
//        sendCommand(Fields.CMD_RESET_ENGINE_SNIFFER);
        log.info("AUTOTEST RPM change [" + rpm + "] executed in " + (System.currentTimeMillis() - time));
    }

    public static void awaitRpm(int rpm) {
        final CountDownLatch rpmLatch = new CountDownLatch(1);

        SensorCentral.ListenerToken listenerToken = SensorCentral.getInstance().addListener(Sensor.RPMValue, actualRpm -> {
            if (isCloseEnough(rpm, actualRpm))
                rpmLatch.countDown();
        });

        // Wait for RPM to change
        try {
            rpmLatch.await(40, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }

        // We don't need to listen to RPM anymore
        listenerToken.remove();
    }

    private static void waitForFirstResponse() throws InterruptedException {
        final CountDownLatch startup = new CountDownLatch(1);
        long waitStart = System.currentTimeMillis();

        ISensorCentral.ListenerToken listener = SensorCentral.getInstance().addListener(Sensor.RPMValue, value -> startup.countDown());
        boolean haveResponse = startup.await(60, TimeUnit.SECONDS);
        if (!haveResponse)
            throw new IllegalStateException("No response from simulator");
        listener.remove();
        FileLog.MAIN.logLine("Got first signal in " + (System.currentTimeMillis() - waitStart) + "ms");
    }

    public static void connectToSimulator(LinkManager linkManager, boolean startProcess) throws InterruptedException {
        if (startProcess) {
            if (FileLog.isWindows()) {
                // this check seems not to work on Linux
                if (!TcpConnector.getAvailablePorts().isEmpty())
                    throw new IllegalStateException("Port already binded on startup?");
            }
            SimulatorExecHelper.startSimulator();
        }


//        FileLog.rlog("Waiting for TCP port...");
//        for (int i = 0; i < 180; i++) {
//            if (!TcpConnector.getAvailablePorts().isEmpty())
//                break;
//            Thread.sleep(1000);
//        }
//        if (TcpConnector.getAvailablePorts().isEmpty())
//            throw new IllegalStateException("Did we start it?");
//        /**
//         * If we open a connection just to validate that the process has started, we are getting
//         * weird issues with the second - actual connection
//         */
//        FileLog.rlog("Time for simulator to close the port...");
//        Thread.sleep(3000);
//
//        FileLog.rlog("Got a TCP port! Connecting...");

        /**
         * TCP connector is blocking
         */
        linkManager.startAndConnect("" + TcpConnector.DEFAULT_PORT, ConnectionStateListener.VOID);
        linkManager.getEngineState().registerStringValueAction(Fields.PROTOCOL_VERSION_TAG, (EngineState.ValueCallback<String>) EngineState.ValueCallback.VOID);
        waitForFirstResponse();
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void sleepSeconds(int seconds) {
        FileLog.MAIN.logLine("Sleeping " + seconds + " seconds");
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void realHardwareConnect(LinkManager linkManager, String port) {
        linkManager.getEngineState().registerStringValueAction(Fields.PROTOCOL_OUTPIN, (EngineState.ValueCallback<String>) EngineState.ValueCallback.VOID);
        linkManager.getEngineState().registerStringValueAction(AverageAnglesUtil.KEY, (EngineState.ValueCallback<String>) EngineState.ValueCallback.VOID);

        try {
            linkManager.connect(port).await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Not connected in time");
        }
    }

    @NotNull
    public static String setTriggerType(trigger_type_e triggerType) {
        return "set " + "trigger_type" + " " + triggerType.ordinal();
    }
}
