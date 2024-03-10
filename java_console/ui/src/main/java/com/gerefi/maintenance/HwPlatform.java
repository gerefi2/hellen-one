package com.gerefi.maintenance;

public enum HwPlatform {
    F4("stm32f4discovery.cfg"),
    F7("st_nucleo_f7.cfg"),
    H7("st_nucleo_h743zi.cfg"),
    ;

    private final String openOcdName;

    HwPlatform(String openOcdName) {
        this.openOcdName = openOcdName;
    }

    public String getOpenOcdName() {
        return openOcdName;
    }

    public static HwPlatform resolve(String value) {
        if (F7.name().equalsIgnoreCase(value))
            return F7;
        return F4;
    }
}
