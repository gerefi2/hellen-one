package com.gerefi.output;

import com.gerefi.ReaderState;
import com.gerefi.ToolUtil;
import com.gerefi.util.LazyFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static com.gerefi.ToolUtil.EOL;

/**
 * This class generates java representation of gerefi data structures used by gerefi console
 */
public class FileJavaFieldsConsumer extends JavaFieldsConsumer {
    private static final String JAVA_PACKAGE = "com.gerefi.config.generated";

    private final LazyFile javaFields;
    private final String className;

    public FileJavaFieldsConsumer(ReaderState state, String javaDestination, int baseOffset, LazyFile.LazyFileFactory fileFactory) {
        super(state, baseOffset);
        javaFields = fileFactory.create(javaDestination);
        String className = new File(javaDestination).getName();
        this.className = remoteExtension(className);
    }

    @NotNull
    public static String remoteExtension(String fileNameWithExtension) {
        return fileNameWithExtension.substring(0, fileNameWithExtension.indexOf('.'));
    }

    @Override
    public void startFile() {
        javaFields.write("package " + JAVA_PACKAGE + ";" + ToolUtil.EOL + ToolUtil.EOL);
        javaFields.write("// this file " + state.getHeader() + ToolUtil.EOL + EOL);
        javaFields.write("// by " + getClass() + EOL);
        javaFields.write("import com.gerefi.config.*;" + EOL + EOL);
        javaFields.write("public class " + className + " {" + ToolUtil.EOL);
    }

    @Override
    public void endFile() throws IOException {
        javaFields.write(state.getVariableRegistry().getJavaConstants());
        javaFields.write(getContent());

        if (allFields.length() > 0) {
            javaFields.write("\tpublic static final Field[] VALUES = {" + EOL);
            allFields.append("\t};" + EOL);
            javaFields.write(allFields.toString());
        }

        javaFields.write("}" + ToolUtil.EOL);
        javaFields.close();
    }
}
