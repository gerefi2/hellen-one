package com.gerefi.output;

import com.gerefi.*;
import com.gerefi.newparse.DefinitionsState;
import com.gerefi.newparse.parsing.Definition;
import com.gerefi.util.SystemOut;

import java.io.IOException;

/**
 * Configuration consumer which writes Signature header file
 */
public class SignatureConsumer implements ConfigurationConsumer {
    private final String destHeader;
    private final VariableRegistry registry;

    public SignatureConsumer(String destHeader, VariableRegistry registry) {
        SystemOut.println("Writing Signature header to " + destHeader);
        this.destHeader = destHeader;
        this.registry = registry;
    }

    public static void storeUniqueBuildId(ReaderStateImpl state, DefinitionsState parseState, String tsPath, FirmwareVersion uniqueId) {
        // store a hash as a built-in variable

        // nasty trick - do not insert signature into live data files
        if (tsPath != null) {
            parseState.addDefinition(state.getVariableRegistry(),
                    ConfigDefinition.SIGNATURE_HASH, uniqueId.encode(), Definition.OverwritePolicy.NotAllowed);
        }
    }

    @Override
    public void handleEndStruct(ReaderState readerState, ConfigStructure structure) throws IOException {
      CHeaderConsumer.writeDefinesToFile(registry, ConfigDefinitionRootOutputFolder.getValue() + destHeader, "by SignatureConsumer");
    }
}
