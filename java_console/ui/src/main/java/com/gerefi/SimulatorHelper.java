package com.gerefi;

import com.gerefi.io.LinkManager;
import com.gerefi.io.tcp.TcpConnector;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

import static com.gerefi.ui.util.UiUtils.setToolTip;

public class SimulatorHelper {
    private final static ThreadFactory THREAD_FACTORY = new NamedThreadFactory("SimulatorHelper");
    public static final String BINARY = "gerefi_simulator.exe";
    private static Process process;

    public static boolean isBinaryHere() {
        return new File(BINARY).exists();
    }

    /**
     * this code start sumulator for UI console
     * todo: unify with the code which starts simulator for auto tests?
     */
    private static void startSimulator() {
        LinkManager.isSimulationMode = true;

        FileLog.MAIN.logLine("Executing " + BINARY);
        THREAD_FACTORY.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileLog.SIMULATOR_CONSOLE.start();
                    process = Runtime.getRuntime().exec(BINARY);
                    FileLog.MAIN.logLine("Executing " + BINARY + "=" + process);
                    SimulatorExecHelper.dumpProcessOutput(process, new CountDownLatch(1));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }).start();

        // unfortunately at this point Windows might popup a dialog asking for a permission to listen to port

        boolean isPortOpened = false;
        for (int i = 0; i < 60 && !isPortOpened; i++) {
            isPortOpened = TcpConnector.isTcpPortOpened();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Unexpected", e);
            }
        }
        if (!isPortOpened)
            throw new IllegalStateException("Port not opened?");
        FileLog.MAIN.logLine("Port " + TcpConnector.DEFAULT_PORT + " is alive");

        new ConsoleUI("" + TcpConnector.DEFAULT_PORT);
    }

    public static JComponent createSimulatorComponent(final StartupFrame portSelector) {
        if (!SimulatorHelper.isBinaryHere())
            return new JLabel(SimulatorHelper.BINARY + " not found");

        if (TcpConnector.isTcpPortOpened())
            return new JLabel("Port " + TcpConnector.DEFAULT_PORT + " already busy. Simulator running?");

        JButton simulatorButton = new JButton("Start Virtual Simulator");
        simulatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                portSelector.disposeFrameAndProceed();
                startSimulator();
            }
        });
        setToolTip(simulatorButton, "Connect to totally virtual simulator",
                "You do not need any hardware to try gerefi",
                "This works via localhost: " + TcpConnector.DEFAULT_PORT + " TCP/IP port");

        return simulatorButton;
    }

    public static void onWindowClosed() {
        if (process != null)
            process.destroy();
    }
}