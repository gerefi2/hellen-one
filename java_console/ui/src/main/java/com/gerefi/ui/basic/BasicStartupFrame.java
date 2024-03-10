package com.gerefi.ui.basic;

import com.gerefi.Launcher;
import com.gerefi.autodetect.PortDetector;
import com.gerefi.core.ui.FrameHelper;
import com.gerefi.maintenance.DfuFlasher;
import com.gerefi.maintenance.ProgramSelector;
import com.gerefi.maintenance.UpdateStatusWindow;
import com.gerefi.ui.LogoHelper;
import com.gerefi.ui.util.HorizontalLine;
import com.gerefi.ui.util.UiUtils;
import com.gerefi.ui.widgets.ToolButtons;
import org.putgemin.VerticalFlowLayout;

import javax.swing.*;

import static com.gerefi.FileLog.isWindows;

/**
 * Much simpler than {@link com.gerefi.StartupFrame}
 */
public class BasicStartupFrame {
    private final FrameHelper frame;

    public static void runTool(String[] args) {
        new BasicStartupFrame().runTool();
    }

    public BasicStartupFrame() {
        String title = "gerefi basic console " + Launcher.CONSOLE_VERSION;
        frame = FrameHelper.createFrame(title);
        JPanel panel = new JPanel(new VerticalFlowLayout());
        if (isWindows()) {
            panel.add(ToolButtons.createShowDeviceManagerButton());

            JButton update = ProgramSelector.createUpdateFirmwareButton();
            update.addActionListener(e -> DfuFlasher.doAutoDfu(update, PortDetector.AUTO, new UpdateStatusWindow("Update")));
            panel.add(update);
        } else {
            panel.add(new JLabel("Sorry only works on Windows"));
        }

        panel.add(new HorizontalLine());
        JLabel logoLabel = LogoHelper.createLogoLabel();
        if (logoLabel != null)
            panel.add(logoLabel);
        panel.add(LogoHelper.createUrlLabel());

        frame.showFrame(panel, false);
        UiUtils.centerWindow(frame.getFrame());
    }

    private void runTool() {
    }
}
