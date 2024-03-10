package com.gerefi.ui.test;

import com.gerefi.config.generated.Fields;
import com.gerefi.ui.engine.EngineSnifferPanel;
import com.gerefi.ui.engine.NameUtil;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static junit.framework.Assert.assertEquals;

/**
 * 6/25/14
 * Andrey Belomutskiy, (c) 2013-2020
 */

public class EngineSnifferPanelTest {
    @Test
    public void testInsertIndex() {
        Set<String> names = new TreeSet<>();
        names.add(Fields.PROTOCOL_CRANK1);

        String name;

        name = "inj2";
        assertEquals(-1, EngineSnifferPanel.getInsertIndex(name, names));
        names.add(name);

        name = "inj1";
        assertEquals(0, EngineSnifferPanel.getInsertIndex(name, names));
        names.add(name);


        assertEquals(0, EngineSnifferPanel.getInsertIndex("spa1", names));
    }
}

