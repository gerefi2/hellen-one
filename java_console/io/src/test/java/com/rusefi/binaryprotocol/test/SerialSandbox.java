package com.gerefi.binaryprotocol.test;

import com.gerefi.autodetect.PortDetector;
import com.gerefi.autodetect.SerialAutoChecker;
import com.gerefi.io.HeartBeatListeners;
import com.gerefi.io.LinkManager;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SerialSandbox {
    public static void main(String[] args) {
        boolean textPull = false;
        long startTime = System.currentTimeMillis();

        SerialAutoChecker.AutoDetectResult autoDetectResult = PortDetector.autoDetectSerial(callbackContext -> null);
        String port = autoDetectResult.getSerialPort();
        System.out.println("Serial detected on " + port);

        HeartBeatListeners.INSTANCE.addListener(() -> {
            int seconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
            System.out.println(new Date() + ": onDataArrival alive for " + seconds + " second(s) textPull=" + textPull + " " + autoDetectResult.getSignature());
        });

        LinkManager linkManager = new LinkManager()
                .setNeedPullText(textPull) // todo: open issue #2
                .setNeedPullLiveData(true);

        try {
            linkManager.connect(port).await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Not connected in time");
        }
    }
}
