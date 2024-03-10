package com.gerefi.sensor_logs;

import com.opensr5.ConfigurationImage;
import com.opensr5.Logger;
import com.gerefi.FileLog;
import com.gerefi.Launcher;
import com.gerefi.binaryprotocol.BinaryProtocol;
import com.gerefi.config.generated.Fields;
import com.gerefi.core.Sensor;
import com.gerefi.core.SensorCentral;
import com.gerefi.ui.UIContext;
import com.gerefi.ui.config.ConfigUiField;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Legacy human readable CSV log file
 * </p>
 * Andrey Belomutskiy, (c) 2013-2020
 */
public class PlainTextSensorLog implements SensorLog {
    private final UIContext uiContext;
    private Writer logFile;

    private long fileStartTime;

    public PlainTextSensorLog(UIContext uiContext) {
        this.uiContext = uiContext;
    }

    private void startIfNeeded() {
        if (logFile == null) {
            /*
             * we only start file header once we have first bunch of data
             */
            startSensorLogFile();
        }
    }

    private void startSensorLogFile() {
        FileLog.createFolderIfNeeded();
        String fileName = Logger.DIR + "gerefi_gauges_" + FileLog.getDate() + ".msl";

        fileStartTime = System.currentTimeMillis();
        try {
            logFile = new FileWriter(fileName);

            logFile.write("\"gerefi console" + Launcher.CONSOLE_VERSION + " firmware " + Launcher.firmwareVersion.get() + "\"\r\n");
            logFile.write("Captured " + FileLog.getDate() + "\r\n");

            int debugMode = -1;
            BinaryProtocol bp = uiContext.getLinkManager().getCurrentStreamState();
            if (bp != null) {
                ConfigurationImage ci = bp.getControllerConfiguration();
                if (ci != null) {
                    debugMode = ConfigUiField.getIntValue(ci, Fields.DEBUGMODE);
                }
            }
            // todo: kill debug mode!
            System.out.println("debug mode " + debugMode);
            logFile.write("Time\t");
            for (Sensor sensor : SensorLogger.SENSORS) {
                logFile.write(SensorLogger.getSensorName(sensor, debugMode) + "\t");
            }
            logFile.write("\r\n");

            logFile.write("Time\t");
            for (Sensor sensor : SensorLogger.SENSORS) {
                logFile.write(sensor.getUnits() + "\t");
            }
            logFile.write("\r\n");
            logFile.flush();


        } catch (IOException e) {
            e.printStackTrace();
            logFile = null;
        }
    }

    @Override
    public double getSecondsSinceFileStart() {
        long msSinceFileStart = System.currentTimeMillis() - fileStartTime;
        return msSinceFileStart / 1000.0;
    }

    @Override
    public void writeSensorLogLine() {
        startIfNeeded();
        if (logFile == null)
            return;

        try {
            logFile.write(getSecondsSinceFileStart() + "\t");
            for (Sensor sensor : SensorLogger.SENSORS) {
                logFile.write( sensor.getLogValue(SensorCentral.getInstance().getValue(sensor)) + "\t");
            }
            logFile.write("\r\n");
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
            logFile = null;
        }
    }

    @Override
    public void close() {
        BinarySensorLog.close(logFile);
        logFile = null;
    }
}

