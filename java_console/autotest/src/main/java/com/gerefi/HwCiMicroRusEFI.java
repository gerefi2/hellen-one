package com.gerefi;

public class HwCiMicrogerefi {
    public static void main(String[] args) {
        CmdJUnitRunner.runHardwareTestAndExit(new Class[]{
                MreHighRevTest.class,
        });
    }
}
