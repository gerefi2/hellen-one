package com.gerefi.ui.lua;

import com.opensr5.ConfigurationImage;
import com.gerefi.ConnectionTab;
import com.gerefi.binaryprotocol.BinaryProtocol;
import com.gerefi.config.generated.Fields;
import com.gerefi.io.ConnectionStatusLogic;
import com.gerefi.io.LinkManager;
import com.gerefi.ui.MessagesPanel;
import com.gerefi.ui.UIContext;
import com.gerefi.core.preferences.storage.Node;
import com.gerefi.ui.util.URLLabel;
import com.gerefi.ui.widgets.AnyCommand;
import neoe.formatter.lua.LuaFormatter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.gerefi.ui.util.UiUtils.trueLayout;

public class LuaScriptPanel {
    private static final String SCRIPT_FOLDER_CONFIG_KEY = "SCRIPT_FOLDER";
    private final UIContext context;
    private final Node config;
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final AnyCommand command;
    private final TextEditor scriptText = new TextEditor();
    private final MessagesPanel mp;

    public LuaScriptPanel(UIContext context, Node config) {
        this.context = context;
        this.config = config;
        ConnectionTab.installConnectAndDisconnect(context, mainPanel);
        command = AnyCommand.createField(context, config, true, true);

        // Upper panel: command entry, etc
        JPanel upperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JButton writeButton = new JButton("Write to ECU");
        JButton burnButton = new JButton("Burn to ECU");

        mp = new MessagesPanel(null, config);

        mp.getMessagesView().listener = message -> {
            if (message.contains("BEEP"))
                Toolkit.getDefaultToolkit().beep();
        };

        writeButton.addActionListener(e -> {
            writeScriptToEcu();
        });

        burnButton.addActionListener(e -> {
            LinkManager linkManager = context.getLinkManager();

            linkManager.submit(() -> {
                BinaryProtocol bp = linkManager.getCurrentStreamState();
                bp.burn();
            });
        });

        JButton moreButton = new JButton("More...");
        moreButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent me) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem format = new JMenuItem("Format Script");
                format.addActionListener(e -> formatScript());
                JMenuItem reset = new JMenuItem("Reset Lua");
                reset.addActionListener(e -> resetLua());


                String scriptName = LuaIncludeSyntax.getScriptName(getScript());
                JMenuItem loadFromDisc;
                if (scriptName == null) {
                    loadFromDisc = new JMenuItem("Script name not specified");
                    loadFromDisc.setEnabled(false);
                } else if (!new File(getScriptFullFileName()).exists()) {
                    loadFromDisc = new JMenuItem(scriptName + " not found in " + getWorkingFolder());
                    loadFromDisc.setEnabled(false);
                } else {
                    loadFromDisc = new JMenuItem("Reload " + scriptName);
                    loadFromDisc.addActionListener(e -> reloadFromDisc());
                }

                JMenuItem selectFolder = createSelectFolderMenuItem();


                menu.add(format);
                menu.add(reset);
                menu.add(new JSeparator());
                menu.add(loadFromDisc);
                menu.add(selectFolder);


                menu.show(moreButton, me.getX(), me.getY());
            }
        });


        upperPanel.add(writeButton);
        upperPanel.add(burnButton);
        upperPanel.add(moreButton);
        upperPanel.add(command.getContent());
        upperPanel.add(new URLLabel("Lua Wiki", "https://github.com/gerefi/gerefi/wiki/Lua-Scripting"));

        // Center panel - script editor and log
        JPanel scriptPanel = new JPanel(new BorderLayout());
        scriptPanel.add(scriptText.getControl(), BorderLayout.CENTER);

        //centerPanel.add(, BorderLayout.WEST);
        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.add(BorderLayout.NORTH, mp.getButtonPanel());
        messagesPanel.add(BorderLayout.CENTER, mp.getMessagesScroll());

        ConnectionStatusLogic.INSTANCE.addListener(isConnected -> {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        readFromECU();
                    } catch (Throwable e) {
                        System.out.println(e);
                    }
                }
            });
        });

        JSplitPane centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scriptPanel, messagesPanel);

        mainPanel.add(upperPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        trueLayout(mainPanel);
        SwingUtilities.invokeLater(() -> centerPanel.setDividerLocation(centerPanel.getSize().width / 2));
    }

    private String getScriptFullFileName() {
        String scriptName = LuaIncludeSyntax.getScriptName(getScript());
        if (scriptName == null)
            return null;
        return getWorkingFolder() + File.separator + scriptName;
    }

    private void reloadFromDisc() {
        String fullFileName = getScriptFullFileName();
        if (fullFileName == null)
            return;
        System.out.println("Reading " + fullFileName);

        try {
            String discContent = Files.readString(new File(fullFileName).toPath());

            String newLua = LuaIncludeSyntax.reloadScript(discContent, name -> {
                String includeFullName = getWorkingFolder() + File.separator + name;
                try {
                    return Files.readString(new File(includeFullName).toPath());
                } catch (IOException e) {
                    return "ERROR reading " + name + ": " + e.getMessage();
                }
            });

            setText(newLua);
            // and send to ECU (without burn!)
            writeScriptToEcu();
        } catch (IOException e) {
            System.err.println("Error " + e);
        }
    }

    @NotNull
    private JMenuItem createSelectFolderMenuItem() {
        JMenuItem selectFolder = new JMenuItem("Select Working Folder");
        selectFolder.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File(getWorkingFolder()));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showSaveDialog(selectFolder);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String newWorkingFolder = fc.getSelectedFile().getPath();
                config.setProperty(SCRIPT_FOLDER_CONFIG_KEY, newWorkingFolder);
            }

        });
        return selectFolder;
    }

    private void formatScript() {
        String sourceCode = getScript();
        try {
            String formatted = new LuaFormatter().format(sourceCode, new LuaFormatter.Env());
            setText(formatted);
        } catch (Exception ignored) {
            // todo: fix luaformatter no reason for exception
        }
    }

    private void setText(String luaScript) {
        scriptText.setText(luaScript);
    }

    private String getWorkingFolder() {
        return config.getProperty(SCRIPT_FOLDER_CONFIG_KEY, System.getProperty("user.home"));
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public ActionListener getTabSelectedListener() {
        return e -> {
            if (command != null)
                command.requestFocus();
        };
    }

    void readFromECU() {
        BinaryProtocol bp = context.getLinkManager().getCurrentStreamState();

        if (bp == null) {
            setText("No ECU located");
            return;
        }

        ConfigurationImage image = bp.getControllerConfiguration();
        if (image == null) {
            setText("No configuration image");
            return;
        }
        ByteBuffer luaScriptBuffer = image.getByteBuffer(Fields.LUASCRIPT.getOffset(), Fields.LUA_SCRIPT_SIZE);

        byte[] scriptArr = new byte[Fields.LUA_SCRIPT_SIZE];
        luaScriptBuffer.get(scriptArr);

        int i = findNullTerminator(scriptArr);
        setText(new String(scriptArr, 0, i, StandardCharsets.US_ASCII));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static int findNullTerminator(byte[] scriptArr) {
        int i;
        for (i = 0; i < scriptArr.length && scriptArr[i] != 0; i++) ;
        return i;
    }

    private void writeScriptToEcu() {
        String script = getScript();

        LinkManager linkManager = context.getLinkManager();

        linkManager.submit(() -> {
            BinaryProtocol bp = linkManager.getCurrentStreamState();

            byte[] paddedScript = new byte[Fields.LUA_SCRIPT_SIZE];
            byte[] scriptBytes = script.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(scriptBytes, 0, paddedScript, 0, scriptBytes.length);

            int idx = 0;
            int remaining;

            do {
                remaining = paddedScript.length - idx;
                int thisWrite = Math.min(remaining, Fields.BLOCKING_FACTOR);

                bp.writeData(paddedScript, idx, Fields.LUASCRIPT.getOffset() + idx, thisWrite);

                idx += thisWrite;

                remaining -= thisWrite;
            } while (remaining > 0);

// need a way to modify script on the fly with shorter execution gaps to keep E65 CAN network happy
// todo: auto-burn on console close check box in case of Lua changes?
// todo: check box for auto-burn?
//            bp.burn();

            // Burning doesn't reload lua script, so we have to do it manually
            resetLua();
        });
        // resume messages on 'write new script to ECU'
        mp.setPaused(false);
    }

    private String getScript() {
        String script = scriptText.getText();
        return script;
    }

    void resetLua() {
        this.context.getCommandQueue().write("luareset");
    }
}
