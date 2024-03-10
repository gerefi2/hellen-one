package com.gerefi.output;

import com.gerefi.ConfigField;
import com.gerefi.ReaderState;

import java.util.List;

public abstract class FieldsStrategy {
    public int run(ReaderState state, ConfigStructure structure, int sensorTsPosition) {
        if (state.isStackEmpty()) {
            return writeFields(structure.getTsFields(), "", sensorTsPosition);
        }
        return sensorTsPosition;
    }

    protected int writeFields(List<ConfigField> tsFields, String prefix, int tsPosition) {
        FieldIterator iterator = new FieldIterator(tsFields);
        for (int i = 0; i < tsFields.size(); i++) {
            iterator.start(i);
            tsPosition = writeOneField(iterator, prefix, tsPosition);

            iterator.end();
        }
        return tsPosition;
    }

    abstract int writeOneField(FieldIterator iterator, String prefix, int tsPosition);

}
