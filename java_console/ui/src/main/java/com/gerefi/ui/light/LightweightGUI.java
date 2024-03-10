package com.gerefi.ui.light;

import com.devexperts.logging.Logging;
import com.gerefi.*;
import com.gerefi.autodetect.PortDetector;
import com.gerefi.core.Sensor;
import com.gerefi.core.SensorCentral;
import com.gerefi.core.io.BundleUtil;
import com.gerefi.core.gerefiVersion;
import com.gerefi.io.ConnectionStateListener;
import com.gerefi.io.ConnectionStatusLogic;
import com.gerefi.io.ConnectionWatchdog;
import com.gerefi.io.LinkManager;
import com.gerefi.ui.LogoHelper;
import com.gerefi.ui.UIContext;
import com.gerefi.core.ui.FrameHelper;
import org.putgemin.VerticalFlowLayout;

import javax.swing.*;
import java.awt.*;

import static com.devexperts.logging.Logging.getLogging;
import static com.gerefi.ui.LogoHelper.createLogoLabel;

public class LightweightGUI {
    private static final Logging log = getLogging(LightweightGUI.class);

    private final JPanel connectedPanel = new JPanel();
    private final JLabel connectedLabel = new JLabel();

    public LightweightGUI(UIContext uiContext) {
        final FrameHelper frameHelper = new FrameHelper();
        frameHelper.getFrame().setTitle("gerefi Lightweight " + gerefiVersion.CONSOLE_VERSION);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        connectedPanel.add(connectedLabel);

        topPanel.add(connectedPanel);

        topPanel.add(new InternetStatus().getContent());


        JPanel leftPanel = new JPanel(new VerticalFlowLayout());
        leftPanel.add(new JLabel(BundleUtil.readBundleFullName()));


        JLabel firmwareVersion = new JLabel();
        SensorCentral.getInstance().addListener(Sensor.FIRMWARE_VERSION, value -> firmwareVersion.setText(Integer.toString((int) value)));
        leftPanel.add(firmwareVersion);

        final JPanel content = new JPanel(new BorderLayout());
        content.add(topPanel, BorderLayout.NORTH);
        content.add(leftPanel, BorderLayout.WEST);

        content.add(createLogoUrlPanel(), BorderLayout.EAST);

        frameHelper.showFrame(content, true);
    }

    public static void start() {

        UIContext uiContext = new UIContext();

        LightweightGUI gui = new LightweightGUI(uiContext);

        gui.setConnectedUI(false);


        new Thread(() -> waitForDeviceAndStart(uiContext.getLinkManager())).start();

        ConnectionStatusLogic.INSTANCE.addListener(new ConnectionStatusLogic.Listener() {
            @Override
            public void onConnectionStatus(boolean isConnected) {
                uiContext.sensorLogger.init();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        gui.setConnectedUI(isConnected);
                    }
                });
            }
        });
    }

    private static JPanel createLogoUrlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(new JLabel(LogoHelper.LINK_TEXT), BorderLayout.SOUTH);
        JLabel logo = createLogoLabel();
        if (logo != null) {
            panel.add(logo, BorderLayout.CENTER);
        }

        return panel;
    }

    private void setConnectedUI(boolean isConnected) {
        connectedLabel.setText(isConnected ? "Connected" : "NOT CONNECTED");
        connectedPanel.setBackground(isConnected ? Color.green : Color.red);
    }

    public static void waitForDeviceAndStart(LinkManager linkManager) {
        String autoDetectedPort = detectPortUntilDetected();
        System.out.println("First time port detected: " + autoDetectedPort);

        linkManager.startAndConnect(autoDetectedPort, ConnectionStateListener.VOID);

        ConnectionWatchdog.init(linkManager);
    }

    private static String detectPortUntilDetected() {
        while (true) {
            String port = PortDetector.autoDetectSerial(null).getSerialPort();
            System.out.println("Detected " + port);
            if (port != null)
                return port;
            IoUtil.sleepSeconds(1);
        }
    }
}
