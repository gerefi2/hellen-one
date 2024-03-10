package com.gerefi.maintenance;

import com.gerefi.io.UpdateOperationCallbacks;

import static com.gerefi.Launcher.INPUT_FILES_PATH;

public class MaintenanceUtil {
    /**
     * Same .bin used by primary DFU and a bit unneeded ST-LINK options
     */
    public static final String FIRMWARE_BIN_FILE = INPUT_FILES_PATH + "/" + "gerefi.bin";

    private static final String WMIC_PCAN_QUERY_COMMAND = "wmic path win32_pnpentity where \"Caption like '%PCAN-USB%'\" get Caption,ConfigManagerErrorCode /format:list";

    static boolean detectDevice(UpdateOperationCallbacks callbacks, String queryCommand, String pattern) {
        //        long now = System.currentTimeMillis();
        StringBuffer output = new StringBuffer();
        StringBuffer error = new StringBuffer();
        ExecHelper.executeCommand(queryCommand, callbacks, output, error, null);
        callbacks.log(output.toString());
        callbacks.log(error.toString());
//        long cost = System.currentTimeMillis() - now;
//        System.out.println("DFU lookup cost " + cost + "ms");
        return output.toString().contains(pattern);
    }

    public static boolean detectPcan(UpdateOperationCallbacks wnd) {
        return detectDevice(wnd, WMIC_PCAN_QUERY_COMMAND, "PCAN");
    }
}
