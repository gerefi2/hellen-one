package com.gerefi.binaryprotocol.test;

import com.opensr5.ConfigurationImage;
import com.opensr5.ini.field.ScalarIniField;
import com.gerefi.TestHelper;
import com.gerefi.binaryprotocol.BinaryProtocolState;
import com.gerefi.config.generated.Fields;
import com.gerefi.io.LinkConnector;
import com.gerefi.io.LinkManager;
import com.gerefi.io.tcp.BinaryProtocolServer;
import com.gerefi.tune.xml.Constant;

import java.io.File;

/**
 * Starts a fake device with TCP connector at {@link BinaryProtocolServer#DEFAULT_PROXY_PORT} port
 * @see TcpServerSandbox what's the difference?
 */
class BinaryProtocolServerSandbox {

    public static void main(String[] args) {
//        File f = new File(BinaryProtocolServer.TEST_FILE);
//        if (!f.exists())
//            throw new IllegalStateException("File not found: " + BinaryProtocolServer.TEST_FILE);

        BinaryProtocolState state = new BinaryProtocolState();
        ConfigurationImage controller = new ConfigurationImage(new byte[Fields.TOTAL_CONFIG_SIZE]);
        ScalarIniField iniField = TestHelper.createIniField(Fields.TWOSTROKE);
        iniField.setValue(controller, new Constant(iniField.getName(), "", "1", iniField.getDigits()));
        state.setController(controller);
        state.setCurrentOutputs(new byte[1 + Fields.TS_TOTAL_OUTPUT_SIZE]);

        LinkManager linkManager = new LinkManager();
        linkManager.setConnector(LinkConnector.getDetachedConnector(state));
        new BinaryProtocolServer().start(linkManager);
    }
}