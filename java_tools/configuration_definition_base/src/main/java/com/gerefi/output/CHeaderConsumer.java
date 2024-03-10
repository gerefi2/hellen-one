package com.gerefi.output;

import com.gerefi.*;
import com.gerefi.util.LazyFile;
import com.gerefi.util.LazyFileImpl;
import com.gerefi.util.SystemOut;

import java.io.IOException;

import static com.gerefi.ToolUtil.EOL;

import org.jetbrains.annotations.NotNull;

/**
 * Configuration consumer which writes C header file
 */
public class CHeaderConsumer extends BaseCHeaderConsumer {
    @NotNull
    private final ReaderState state;
    /**
     * looks like sometimes we want to not include "define XXX value" into generated C headers
     * TODO: document the use-case better
     */
    private final boolean withC_Defines;
    private final LazyFile cHeader;
    private final VariableRegistry variableRegistry;

    public CHeaderConsumer(ReaderState state, String destCHeader, boolean withC_Defines, LazyFile.LazyFileFactory fileFactory) {
        this.variableRegistry = state.getVariableRegistry();
        this.state = state;
        this.withC_Defines = withC_Defines;
        SystemOut.println("Writing C header to " + destCHeader);
        cHeader = fileFactory.create(destCHeader);
    }

    public static void writeDefinesToFile(VariableRegistry variableRegistry, String fileName, String headerComment) throws IOException {

        SystemOut.println("Writing to " + fileName);
        LazyFile cHeader = new LazyFileImpl(fileName);

        cHeader.write("//\n// " + ToolUtil.getGeneratedAutomaticallyTag() + headerComment + "\n//\n\n");
        cHeader.write(variableRegistry.getDefinesSection());
        cHeader.close();
    }

    @Override
    public void startFile() {
        cHeader.write("// this section " + state.getHeader() + EOL);
        cHeader.write("// by " + getClass() + EOL);
        cHeader.write("// begin" + EOL);
        cHeader.write("#pragma once" + EOL);
        cHeader.write("#include \"gerefi_types.h\"" + EOL);
    }

    @Override
    public void endFile() throws IOException {
        if (withC_Defines)
            cHeader.write(variableRegistry.getDefinesSection());
        cHeader.write(getContent().toString());
        cHeader.write("// end" + EOL);
        cHeader.write("// this section " + state.getHeader() + EOL);
        cHeader.close();
    }
}
