package com.gerefi.autodetect;

import com.gerefi.io.ConnectionStatusLogic;
import com.gerefi.io.LinkManager;
import com.gerefi.ui.light.LightweightGUI;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReconnectSandbox {
    public static void main(String[] args) throws InterruptedException {

        LinkManager linkManager = new LinkManager();

        LightweightGUI.waitForDeviceAndStart(linkManager);

        AtomicBoolean status = new AtomicBoolean();

        ConnectionStatusLogic.INSTANCE.addListener(isConnected -> status.set(isConnected));

        while (true) {
            System.out.println("Hello " + status);
            Thread.sleep(1000);
        }
    }

}
