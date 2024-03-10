package com.gerefi.waves.test;

import com.gerefi.waves.RevolutionLog;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Andrey Belomutskiy (c) 2012-2014
 * 3/19/14
 */
public class RevolutionLogTest {
    @Test
    public void backTime() {
        RevolutionLog r = RevolutionLog.parseRevolutions("2000!148958!2000!154958!2000!160958!2000!166958!");

        assertEquals(594.84, r.getCrankAngleByTime(147915));

        // too back into the past
        assertEquals(Double.NaN, r.getCrankAngleByTime(140915));
    }
}
