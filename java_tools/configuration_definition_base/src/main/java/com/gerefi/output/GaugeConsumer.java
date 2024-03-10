package com.gerefi.output;

import com.gerefi.ConfigField;
import com.gerefi.ConfigFieldImpl;
import com.gerefi.ReaderState;
import com.gerefi.VariableRegistry;
import com.gerefi.util.LazyFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.gerefi.ldmp.LiveDataProcessor.tempLimit;
import static com.gerefi.output.DataLogConsumer.getHumanGaugeName;

public class GaugeConsumer implements ConfigurationConsumer {
    private final String fileName;
    private final LazyFile.LazyFileFactory fileFactory;
    private final LinkedHashMap<String, StringBuilder> byCategory = new LinkedHashMap<>();
    public String[] outputNames = new String[]{""};

    public GaugeConsumer(String fileName, LazyFile.LazyFileFactory fileFactory) {
        this.fileName = fileName;
        this.fileFactory = fileFactory;
    }

    @Override
    public void handleEndStruct(ReaderState readerState, ConfigStructure structure) throws IOException {
        if (readerState.isStackEmpty()) {
            for (int i = 0; i < tempLimit(outputNames); i++) {

                String variableNameSuffix = outputNames.length > 1 ? Integer.toString(i) : "";

                PerFieldWithStructuresIterator iterator = new PerFieldWithStructuresIterator(readerState, structure.getTsFields(), "",
                        (state, configField, prefix) -> handle(configField, prefix, variableNameSuffix));
                iterator.loop();
            }
        }
    }

    @Override
    public void endFile() throws IOException {
        if (fileName != null) {
            LazyFile fw = fileFactory.create(fileName);
            fw.write(getContent());
            fw.close();
        }
    }

    private String handle(ConfigField configField, String prefix, String variableNameSuffix) {
        String comment = getHumanGaugeName("", configField, variableNameSuffix);
        comment = ConfigFieldImpl.unquote(comment);
        if (!prefix.isEmpty()) {
            comment = prefix + " " + comment;
        }
        comment = VariableRegistry.quote(comment);


        double min = configField.getMin();
        double max = configField.getMax();
        int digits = configField.getDigits();
        String category = configField.getCategory();
        if (category == null)
            return "";

        StringBuilder sb = byCategory.computeIfAbsent(category, s -> new StringBuilder());

        String fullName = prefix + configField.getName();
        String gaugeEntry = fullName + variableNameSuffix + "Gauge = " + fullName + variableNameSuffix + "," + comment +
                ", " + VariableRegistry.quote(configField.getUnits()) +
                ", " + min + "," + max +
                ", " + min + "," + max +
                ", " + min + "," + max +
                ", " + digits + "," + digits +
                "\n";
        sb.append(gaugeEntry);

        return "";
    }

    public String getContent() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, StringBuilder> e : byCategory.entrySet()) {
            sb.append("\t").append("gaugeCategory = ").append(e.getKey()).append("\n");
            sb.append(e.getValue());
        }

        return sb.toString();
    }
}
