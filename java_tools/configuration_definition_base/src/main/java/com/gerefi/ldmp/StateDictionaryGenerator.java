package com.gerefi.ldmp;

import com.gerefi.ToolUtil;
import com.gerefi.output.FileJavaFieldsConsumer;
import com.gerefi.util.LazyFile;

import java.io.IOException;
import java.util.Date;

import static com.gerefi.VariableRegistry.quote;

public class StateDictionaryGenerator implements LiveDataProcessor.EntryHandler {
    public final StringBuilder content = new StringBuilder();
    private final String yamlFileName;

    public StateDictionaryGenerator(String yamlFileName) {
        this.yamlFileName = yamlFileName;
    }

    @Override
    public void onEntry(String name, String javaName, String folder, String prepend, boolean withCDefines, String[] outputNames, String constexpr, String conditional, String engineModule, Boolean isPtr, String cppFileName) throws IOException {
        content.append("        stateDictionary.register(live_data_e.LDS_");
        content.append(name).append(", ");

        content.append(FileJavaFieldsConsumer.remoteExtension(javaName)).append(".VALUES, ");
        content.append(quote(cppFileName));

        content.append(");\n");
    }

    public String getCompleteClass() {
        ToolUtil.TOOL = getClass().getSimpleName();

        return "package com.gerefi.enums;\n" +
                "//" + ToolUtil.getGeneratedAutomaticallyTag() + yamlFileName + " on " + new Date() + "n" +
                "\n" +
                "import com.gerefi.config.generated.*;\n" +
                "import com.gerefi.ldmp.StateDictionary;\n" +
                "\n" +
                "public class StateDictionaryFactory {\n" +
                "    public static void initialize(StateDictionary stateDictionary) {\n"
                + content +
                "    }\n" +
                "}";

    }
}
