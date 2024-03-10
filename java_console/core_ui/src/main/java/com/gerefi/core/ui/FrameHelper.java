package com.gerefi.core.ui;

import com.gerefi.core.io.BundleUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

/**
 * Date: 3/24/13
 * Andrey Belomutskiy, (c) 2013-2020
 */
public class FrameHelper {
    private final JFrame frame = new JFrame();

    public FrameHelper() {
        this(JDialog.DISPOSE_ON_CLOSE);
    }

    public FrameHelper(int operation) {
        frame.setDefaultCloseOperation(operation);
        AutoupdateUtil.setAppIcon(frame);
    }

    @NotNull
    public static String appendBundleName(String title) {
        String bundleName = BundleUtil.readBundleFullNameNotNull();
        return title + " " + bundleName;
    }

    public static FrameHelper createFrame(String title) {
        FrameHelper frame = new FrameHelper();
        frame.frame.setTitle(appendBundleName(title));
        return frame;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void showFrame(JComponent component) {
        showFrame(component, true);
    }

    public void showFrame(JComponent component, final boolean maximizeOnStart) {
        initFrame(component, maximizeOnStart);
        frame.setVisible(true);
    }

    public void initFrame(JComponent component, final boolean maximizeOnStart) {
        frame.setSize(800, 500);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                onWindowOpened();
                if (maximizeOnStart)
                    frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            }

            @Override
            public void windowClosed(WindowEvent ev) {
                onWindowClosed();
                for (Thread t : Thread.getAllStackTraces().keySet()) {
                    if (!t.isDaemon())
                        System.out.println("Non-daemon thread: " + t);
                }
                System.out.println(Arrays.toString(Frame.getFrames()));
            }
        });
        frame.add(component);
    }

    protected void onWindowOpened() {
    }

    protected void onWindowClosed() {
    }
}
