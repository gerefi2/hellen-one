package com.gerefi.ui;

import com.opensr5.ini.IniFileModel;
import com.gerefi.SensorSnifferCentral;
import com.gerefi.io.CommandQueue;
import com.gerefi.io.LinkManager;
import com.gerefi.sensor_logs.SensorLogger;
import org.jetbrains.annotations.NotNull;

public class UIContext {
    private final LinkManager linkManager = new LinkManager();

    public SensorLogger sensorLogger = new SensorLogger(this);
    public GaugesPanel.DetachedRepository DetachedRepositoryINSTANCE = new GaugesPanel.DetachedRepository(this);

    public final SensorSnifferCentral sensorSnifferCentral = new SensorSnifferCentral(linkManager);

    @NotNull
    public LinkManager getLinkManager() {
        return linkManager;
    }

    public CommandQueue getCommandQueue() {
        return linkManager.getCommandQueue();
    }

    public IniFileModel getIni() {
        return IniFileModel.getInstance();
    }
}
