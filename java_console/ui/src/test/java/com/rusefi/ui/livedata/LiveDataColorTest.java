package com.gerefi.ui.livedata;

import com.gerefi.CodeWalkthrough;
import com.gerefi.enums.live_data_e;
import com.gerefi.ldmp.StateDictionary;
import com.gerefi.livedata.LiveDataParserPanel;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.gerefi.livedata.LiveDataParserPanel.getContentOrNull;
import static com.gerefi.ui.LiveDataPane.CPP_SUFFIX;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Ignore // todo: https://github.com/gerefi/gerefi/issues/4669
public class LiveDataColorTest {
    @Test
    public void testAllFiles() throws IOException {
        int counter = 0;
        for (live_data_e view : live_data_e.values()) {
            String fileName = StateDictionary.INSTANCE.getFileName(view) + CPP_SUFFIX;

            try {
                testSpecificFile(fileName);
            } catch (RuntimeException e) {
                throw new IllegalStateException("During " + fileName, e);
            }

            counter++;
        }

        assertTrue(counter > 4);

    }

    private void testSpecificFile(String fileName) throws IOException {
        String sourceCode = getContentOrNull(getClass(), fileName);
        assertNotNull(sourceCode, "Not found: sourceCode for " + fileName);

        ParseTree tree = LiveDataParserPanel.getParseTree(sourceCode);

        CodeWalkthrough.applyVariables(VariableValueSource.VOID, sourceCode, new SourceCodePainter() {
            @Override
            public void paintBackground(Color color, Range range) {
            }

            @Override
            public void paintForeground(Color color, Range range) {
            }
        }, tree);
    }
}
