package com.gerefi.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility class which writes same line both to stdout and file
 */
//@ThreadSafe
public class SystemOut {
    private static final String LOG = ".log";
    private static PrintWriter logFile;

    public static void println(String line) {
        System.out.println(line);
        try {
            openFileIfNeeded();
            logFile.write(line + "\r\n");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static synchronized void openFileIfNeeded() throws IOException {
        if (logFile != null)
            return;
        String fileName = System.getProperty("SystemOut.name", "gerefi_tool") + LOG;
        System.out.println("Opening " + fileName);
        File parentFile = new File(fileName).getParentFile();
        if (parentFile != null)
            parentFile.mkdirs();
        logFile = new PrintWriter(new FileWriter(fileName, true));
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                close();
                System.out.println("SystemOut Hook done for " + fileName);
            }
        }));
    }

    @SuppressWarnings("ThrowablePrintedToSystemOut")
    public static void println(Throwable param) {
        System.out.println(param);
        try {
            openFileIfNeeded();
            param.printStackTrace(logFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void println(Object object) {
        println(object == null ? "(null)" : object.toString());
    }

    public synchronized static void close() {
        if (logFile != null) {
            logFile.close();
        }
        logFile = null;
    }
}
