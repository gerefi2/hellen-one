package com.gerefi.tools.tune;

import com.devexperts.logging.Logging;
import com.opensr5.ConfigurationImage;
import com.opensr5.ini.IniFileModel;
import com.gerefi.binaryprotocol.MsqFactory;
import com.gerefi.config.generated.Fields;
import com.gerefi.tune.xml.Msq;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static com.devexperts.logging.Logging.getLogging;

/**
 * see <a href="https://github.com/gerefi/gerefi/wiki/Canned-Tune-Process">...</a>
 */
public class WriteSimulatorConfiguration {
    private static final Logging log = getLogging(WriteSimulatorConfiguration.class);
    // f407-discovery is historically the most inclusive .ini file
    public static final String INI_FILE_FOR_SIMULATOR = "../firmware/tunerstudio/generated/gerefi_f407-discovery.ini";

    public static String ROOT_FOLDER = System.getProperty("ROOT_FOLDER", "");

    public static void main(String[] args) throws IOException, InterruptedException, JAXBException {
        System.out.println("ROOT_FOLDER=" + ROOT_FOLDER);
        try {
            readBinaryWriteXmlTune(Fields.SIMULATOR_TUNE_BIN_FILE_NAME, TuneCanTool.DEFAULT_TUNE);
            for (int type : new int[]{
                    // [CannedTunes] see 'gerefiFunctionalTest.cpp' which exports default tunes into binary files for us
                    Fields.engine_type_e_MRE_M111,
                    Fields.engine_type_e_HONDA_K,
                    Fields.engine_type_e_HELLEN_154_HYUNDAI_COUPE_BK1,
                    Fields.engine_type_e_HELLEN_154_HYUNDAI_COUPE_BK2,
                    Fields.engine_type_e_HYUNDAI_PB,
                    Fields.engine_type_e_MAVERICK_X3,
                    Fields.engine_type_e_HARLEY,
            }) {
                writeSpecificEngineType(type);
            }
        } catch (Throwable e) {
            log.error("Unfortunately", e);
            System.exit(-1);
        } finally {
            // No way to set Process.exec to be a daemon, we need explicit exit
            System.exit(0);
        }
    }

    private static void writeSpecificEngineType(int engineType) {
        String engine = "_" + engineType;
        try {
            readBinaryWriteXmlTune(Fields.SIMULATOR_TUNE_BIN_FILE_NAME_PREFIX + engine + Fields.SIMULATOR_TUNE_BIN_FILE_NAME_SUFFIX,
                    TuneCanTool.SIMULATED_PREFIX + engine + TuneCanTool.SIMULATED_SUFFIX);
        } catch (Throwable e) {
            throw new IllegalStateException("With " + engineType, e);
        }
    }

    private static void readBinaryWriteXmlTune(String tuneBinFileName, String outputXmlFileName) throws JAXBException, IOException {
        byte[] fileContent = Files.readAllBytes(new File(ROOT_FOLDER + tuneBinFileName).toPath());
        System.out.println("Got " + fileContent.length + " from " + tuneBinFileName + " while expecting " + Fields.TOTAL_CONFIG_SIZE);
        if (fileContent.length != Fields.TOTAL_CONFIG_SIZE)
            throw new IllegalStateException("Unexpected image size " + fileContent.length + " while expecting " + Fields.TOTAL_CONFIG_SIZE);
        ConfigurationImage configuration = new ConfigurationImage(fileContent);
        System.out.println("Got " + Objects.requireNonNull(configuration, "configuration"));
        IniFileModel ini = new IniFileModel().readIniFile(INI_FILE_FOR_SIMULATOR);
        if (ini == null)
            throw new IllegalStateException("Not found " + INI_FILE_FOR_SIMULATOR);
        Msq m = MsqFactory.valueOf(configuration, ini);
        m.writeXmlFile(ROOT_FOLDER + outputXmlFileName);

        Msq newTuneJustToValidate = Msq.readTune(ROOT_FOLDER + outputXmlFileName);
        System.out.println("Looks valid " + newTuneJustToValidate);
    }
}
