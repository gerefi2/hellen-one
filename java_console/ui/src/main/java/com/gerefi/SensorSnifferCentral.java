package com.gerefi;

import com.gerefi.core.EngineState;
import com.gerefi.io.LinkManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SensorSnifferCentral {
    private List<AnalogChartListener> listeners = new CopyOnWriteArrayList<>();

    public SensorSnifferCentral(LinkManager linkManager) {
        linkManager.getEngineState().registerStringValueAction(AverageAnglesUtil.KEY, new EngineState.ValueCallback<String>() {
                    @Override
                    public void onUpdate(String message) {
                        for (AnalogChartListener listener : listeners)
                            listener.onAnalogChart(message);
                    }
                }
        );
    }

    public void addListener(AnalogChartListener listener) {
        listeners.add(listener);
    }

    interface AnalogChartListener {
        void onAnalogChart(String analogChart);
    }
}