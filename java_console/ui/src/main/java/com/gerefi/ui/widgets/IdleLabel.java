package com.gerefi.ui.widgets;

import com.gerefi.core.Sensor;
import com.gerefi.core.SensorCentral;

import javax.swing.*;

/**
 * 8/2/13
 * Andrey Belomutskiy, (c) 2013-2020
 */
public class IdleLabel extends JLabel {
    public IdleLabel() {
//        SensorCentral.getInstance().addListener(Sensor.IDLE_SWITCH, new SensorCentral.SensorListener() {
//            @Override
//            public void onSensorUpdate(double value) {
//                IdleLabel.this.setText("Idle: " + (value == 0));
//            }
//        });
    }
}
