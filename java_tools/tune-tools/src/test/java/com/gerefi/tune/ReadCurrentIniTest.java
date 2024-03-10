package com.gerefi.tune;

import com.opensr5.ini.IniFileModel;
import com.gerefi.tools.tune.WriteSimulatorConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReadCurrentIniTest {
    @Test
    public void test() {
        IniFileModel ini = new IniFileModel().readIniFile("../" + WriteSimulatorConfiguration.INI_FILE_FOR_SIMULATOR);
        Assertions.assertNotNull(ini);
    }
}
