package com.gerefi;

import com.gerefi.functional_tests.EcuTestHelper;
import com.gerefi.waves.EngineChart;
import org.junit.*;

public class gerefiTestBase {
    protected EcuTestHelper ecu;

    protected boolean needsHardwareTriggerInput() {
        // Most tests do not, but some may need it
        return false;
    }

    @Before
    public void startUp() {
        ecu = EcuTestHelper.createInstance(needsHardwareTriggerInput());
    }

    @After
    public void checkStackUsage() {
        if (ecu != null)
            ecu.sendCommand("threadsinfo");
    }

    protected EngineChart nextChart() {
        return TestingUtils.nextChart(ecu.commandQueue);
    }
}
