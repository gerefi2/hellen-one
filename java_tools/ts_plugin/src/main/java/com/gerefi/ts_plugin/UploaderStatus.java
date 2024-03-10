package com.gerefi.ts_plugin;

import com.gerefi.TsTuneReader;
import com.gerefi.tune.xml.Constant;

import java.io.File;
import java.util.Map;

public class UploaderStatus {
    static final String NO_PROJECT = "Please open project";
    public String projectWarning;
    public String tuneInfo;
    public String tuneWarning;

    void updateProjectStatus(String configurationName, boolean isProjectActive) {
        if (!isProjectActive) {
            this.projectWarning = NO_PROJECT;
        } else if (!new File(TsTuneReader.getTsTuneFileName(configurationName)).exists()) {
            this.projectWarning = "Tune not found " + configurationName;
        } else {
            this.projectWarning = null;
        }
    }

    void readTuneState(String configurationName) {
        Map<String, Constant> fileSystemValues = TuneUploder.getFileSystemValues(configurationName);
        Constant engineMake = fileSystemValues.get("enginemake");
        Constant engineCode = fileSystemValues.get("enginecode");
        Constant vehicleName = fileSystemValues.get("VEHICLENAME");
        String warning = "";
        if (PluginEntry.isEmpty(engineMake)) {
            warning += " engine make";
        }
        if (PluginEntry.isEmpty(engineCode)) {
            warning += " engine code";
        }
        if (PluginEntry.isEmpty(vehicleName)) {
            warning += " vehicle name";
        }
        if (warning.isEmpty()) {
            tuneInfo = engineMake.getValue() + " " + engineCode.getValue() + " " + vehicleName.getValue();
            tuneWarning = null;
        } else {
            tuneInfo = null;
            tuneWarning = "<html>Please set " + warning + " on Base Settings tab<br>and reopen Project";
        }
    }

    public boolean isProjectIsOk() {
        return projectWarning == null;
    }

    public boolean isTuneOk() {
        return tuneWarning == null;
    }
}
