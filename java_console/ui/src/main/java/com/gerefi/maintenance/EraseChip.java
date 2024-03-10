package com.gerefi.maintenance;

import com.gerefi.core.ui.AutoupdateUtil;
import com.gerefi.ui.StatusWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

import static com.gerefi.maintenance.StLinkFlasher.TITLE;

/**
 * DEAD?!
 *
 * Andrey Belomutskiy, (c) 2013-2020
 */
public class EraseChip {
    private static final String FLASH_SIZE = "0x0100000";
    private static final String ERASE_COMMAND_SUFFIX = " -c init -c targets -c \"halt\" -c \"flash erase_address 0x08000000 " + FLASH_SIZE + "\" -c shutdown";

    private final JButton button = new JButton("ST-LINK Erase Chip", AutoupdateUtil.loadIcon("erase.png"));
    private final StatusWindow wnd = new StatusWindow();

    public EraseChip() {
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog(button, "Do you really want to reset stm32 chip and all settings? Please disconnect battery before erasing.",
                        "Please disconnect from vehicle", JOptionPane.YES_NO_OPTION);
                if (dialogResult != JOptionPane.YES_OPTION)
                    return;
                wnd.showFrame(TITLE);
                StatusAnimation sa = new StatusAnimation(wnd);
                ExecHelper.submitAction(() -> {
                  try {
                    StLinkFlasher.executeOpenOCDCommand(getEraseCommand(), wnd);
                  } catch (FileNotFoundException ex) {
                    wnd.append(ex.toString());
                    wnd.error();
                  }
                  sa.stop();
                    wnd.setStatus(StLinkFlasher.DONE);
                },  EraseChip.this.getClass() + " extProcessThread");
            }
        });
    }

    @NotNull
    private String getEraseCommand() {
        return StLinkFlasher.getOpenocdCommand() + ERASE_COMMAND_SUFFIX;
    }

    public JButton getButton() {
        return button;
    }
}
