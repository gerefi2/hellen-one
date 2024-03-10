package com.gerefi.ui;

import com.devexperts.logging.Logging;
import com.gerefi.NamedThreadFactory;
import com.gerefi.io.can.PCanIoStream;
import com.gerefi.tools.CANConnectorStartup;
import com.gerefi.core.ui.FrameHelper;
import com.gerefi.ui.util.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static com.devexperts.logging.Logging.getLogging;

public class PcanConnectorUI {
    private static final Logging log = getLogging(PcanConnectorUI.class);

    public static void show() {
        FrameHelper frame = new FrameHelper(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Running PCAN connector for TS"), BorderLayout.NORTH);
        JTextArea logTextArea = new JTextArea();
        panel.add(logTextArea, BorderLayout.CENTER);

        StatusConsumer statusConsumer = string -> SwingUtilities.invokeLater(() -> {
            log.info(string);
            logTextArea.append(string + "\r\n");
            UiUtils.trueLayout(logTextArea);
        });

        new NamedThreadFactory("PCAN-connector").newThread(() -> {
            PCanIoStream stream = PCanIoStream.createStream(statusConsumer);
            try {
                if (stream != null)
                    CANConnectorStartup.start(stream, statusConsumer);
            } catch (IOException e) {
                statusConsumer.append("Error " + e);
            }
        }).start();

        frame.showFrame(panel);
    }
}
