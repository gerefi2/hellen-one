package com.gerefi.ui;

import com.devexperts.logging.Logging;
import com.gerefi.core.io.BundleUtil;
import com.gerefi.core.gerefiVersion;
import com.gerefi.core.ui.FrameHelper;
import com.gerefi.io.UpdateOperationCallbacks;
import com.gerefi.ui.util.UiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

import static com.devexperts.logging.Logging.getLogging;

/**
 * Andrey Belomutskiy, (c) 2013-2020
 * 3/7/2015
 */
public class StatusWindow implements StatusConsumer, UpdateOperationCallbacks {
    private static final Logging log = getLogging(StatusWindow.class);

    private static final Color LIGHT_RED = new Color(255, 102, 102);
    private static final Color LIGHT_GREEN = new Color(102, 255, 102);
    // todo: extract driver from console bundle? find a separate driver bundle?
    private final JTextArea logTextArea = new JTextArea();
    private final JPanel content = new JPanel(new BorderLayout());
    private final JLabel bottomStatusLabel = new JLabel();
    @NotNull
    protected final FrameHelper frameHelper = new FrameHelper();

    public StatusWindow() {
        logTextArea.setLineWrap(true);
        JScrollPane messagesScroll = new JScrollPane(logTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(400, 400);
            }
        };
        content.add(messagesScroll, BorderLayout.CENTER);
        content.add(bottomStatusLabel, BorderLayout.SOUTH);

        append("Console version " + gerefiVersion.CONSOLE_VERSION);
        append("Windows " + System.getProperty("os.version"));
        append("Bundle " + BundleUtil.readBundleFullNameNotNull());
    }

    @NotNull
    public FrameHelper getFrameHelper() {
        return frameHelper;
    }

    public JPanel getContent() {
        return content;
    }


    public void setErrorState() {
        logTextArea.setBackground(LIGHT_RED);
        copyContentToClipboard();
    }

  @Override
  public void log(String message) {
    append(message);

  }

  @Override
  public void done() {
    setSuccessState();

  }

  @Override
  public void error() {
    setErrorState();
  }

  public void setSuccessState() {
        logTextArea.setBackground(LIGHT_GREEN);
    }

    public JFrame getFrame() {
        return frameHelper.getFrame();
    }

    public void showFrame(String title) {
        frameHelper.getFrame().setTitle(title);
        frameHelper.showFrame(content, false);
        UiUtils.centerWindow(frameHelper.getFrame());
        logTextArea.setText(""); // let's remove stuff from previous invocation
    }

    @Override
    public void append(final String string) {
        // todo: check if AWT thread and do not invokeLater if already on AWT thread
        SwingUtilities.invokeLater(() -> {
            String s = string.replaceAll(Character.toString((char) 219), "");
            log.info(s);
            logTextArea.append(s + "\r\n");
            UiUtils.trueLayout(logTextArea);
        });
    }

    public void copyContentToClipboard() {
        // kludge: due to 'append' method using invokeLater even while on AWT thread we also need invokeLater to
        // actually get overall status message
        SwingUtilities.invokeLater(() -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(logTextArea.getText()), null));

        append("hint: error state is already in your clipboard, please use PASTE or Ctrl-V while reporting issues");
    }

    public void setStatus(String status) {
        bottomStatusLabel.setText(status);
    }
}
