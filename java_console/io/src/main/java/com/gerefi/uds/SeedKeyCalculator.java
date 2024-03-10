package com.gerefi.uds;

import com.devexperts.logging.Logging;

import static com.devexperts.logging.Logging.getLogging;

public class SeedKeyCalculator {
    private static Logging log = getLogging(SeedKeyCalculator.class);

    public static final int BOOTLOADER_SECRET = 0xB24F5249;
    public static final int SECRET = 0x57649392;

    public static int Uds_Security_CalcKey(int secret, int seed, int rnd) {
        rnd = rnd & 0xFF;
        int originalSeed = seed;
        if (rnd < 220)
            rnd += 35;
        else
            rnd = 255;

        for (int i = 0; i < rnd; i++) {
            if (seed < 0)
                seed = secret ^ seed << 1;
            else
                seed <<= 1;
        }
        log.info(String.format("seed %x secret %x rnd %x makes %x", originalSeed, secret, rnd, seed));
        return seed;
    }
}
