package com.gerefi.ui.widgets;

import com.gerefi.core.ui.AutoupdateUtil;
import com.gerefi.maintenance.ExecHelper;
import com.gerefi.ui.PcanConnectorUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ToolButtons {
    public static Component createShowDeviceManagerButton() {
        JButton showDeviceManager = new JButton(AutoupdateUtil.loadIcon("DeviceManager.png"));
        showDeviceManager.setMargin(new Insets(0, 0, 0, 0));
        showDeviceManager.setToolTipText("Show Device Manager");
        showDeviceManager.addActionListener(event -> {
            try {
                Runtime.getRuntime().exec(ExecHelper.getBatchCommand("devmgmt.msc"));
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
        return showDeviceManager;
    }

    public static Component createPcanConnectorButton() {
        JButton button = new JButton("PCAN");
        button.setToolTipText("PCAN connector for TS");
        button.addActionListener(e -> PcanConnectorUI.show());
        return button;
    }
}
