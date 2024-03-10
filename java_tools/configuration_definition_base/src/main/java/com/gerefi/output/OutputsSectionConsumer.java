package com.gerefi.output;

import com.devexperts.logging.Logging;
import com.gerefi.ReaderState;
import com.gerefi.util.LazyFile;

import java.io.IOException;

import static com.devexperts.logging.Logging.getLogging;
import static com.gerefi.ldmp.LiveDataProcessor.needComment;
import static com.gerefi.ldmp.LiveDataProcessor.tempLimit;

/**
 * TODO: We have to move either forward or backwards with newparse #4441
 * [OutputChannels]
 */
public class OutputsSectionConsumer implements ConfigurationConsumer {
    private static final Logging log = getLogging(OutputsSectionConsumer.class);

    private final String tsOutputsSectionFileName;
    private final LazyFile.LazyFileFactory fileFactory;
    private final TsOutput tsOutput;
    public String[] outputNames = new String[]{""};
    private int sensorTsPosition;

    public OutputsSectionConsumer(String tsOutputsSectionFileName, LazyFile.LazyFileFactory fileFactory) {
        this.tsOutputsSectionFileName = tsOutputsSectionFileName;
        this.fileFactory = fileFactory;
        tsOutput = new TsOutput(false);
    }

    public String getContent() {
        return tsOutput.getContent();
    }

    public int getBaseOffset() {
        return sensorTsPosition;
    }

    @Override
    public void handleEndStruct(ReaderState readerState, ConfigStructure structure) throws IOException {
        log.info("handleEndStruct");

        for (int i = 0; i < tempLimit(outputNames); i++) {
            String temporaryLineComment = needComment(i) ? ";" : "";

            String variableNameSuffix = outputNames.length > 1 ? Integer.toString(i) : "";


            sensorTsPosition = tsOutput.run(readerState, structure, sensorTsPosition, temporaryLineComment, variableNameSuffix);

            if (readerState.isStackEmpty()) {
                if (tsOutputsSectionFileName != null) {
                    LazyFile fos = fileFactory.create(tsOutputsSectionFileName);
                    fos.write(tsOutput.getContent());
                    fos.close();
                }
            }
        }
    }
}
