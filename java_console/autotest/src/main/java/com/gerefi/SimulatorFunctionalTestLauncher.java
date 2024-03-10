package com.gerefi;

import com.gerefi.io.LinkManager;
import com.gerefi.simulator.SimulatorFunctionalTest;

import java.io.File;
import java.io.IOException;

/**
 * this class runs gerefi functional tests against gerefi simulator
 */
public class SimulatorFunctionalTestLauncher {
    static volatile boolean isHappy;
    public static void main(String[] args) throws IOException, InterruptedException {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            System.exit(66);
        });
        boolean startSimulator = args.length == 1 && args[0].equalsIgnoreCase("start");

//        if (startSimulator) {
//            buildSimulator();
//        }

        long start = System.currentTimeMillis();
        FileLog.SIMULATOR_CONSOLE.start();
        FileLog.MAIN.start();

        boolean failed = false;
        try {
            LinkManager linkManager = new LinkManager();
            IoUtil.connectToSimulator(linkManager, startSimulator);
            TestingUtils.installVoidEngineSnifferAction(linkManager.getCommandQueue());
            new SimulatorFunctionalTest(linkManager).mainTestBody();
        } catch (Throwable e) {
            e.printStackTrace();
            failed = true;
        } finally {
            SimulatorExecHelper.destroy();
        }
        if (failed)
            System.exit(-1);
        isHappy = true;
        FileLog.MAIN.logLine("*******************************************************************************");
        FileLog.MAIN.logLine("**** SimulatorFunctionalTestLauncher  Looks good! *****************************");
        FileLog.MAIN.logLine("*******************************************************************************");
        long time = (System.currentTimeMillis() - start) / 1000;
        FileLog.MAIN.logLine("Done in " + time + "secs");
        System.exit(0); // this is a safer method eliminating the issue of non-daemon threads
    }

    private static void buildSimulator() throws IOException, InterruptedException {
        Process makeProcess = Runtime.getRuntime().exec("make -j8", null, new File("../simulator"));
        SimulatorExecHelper.dumpProcessOutput(makeProcess, null);
        makeProcess.waitFor();
    }
}
