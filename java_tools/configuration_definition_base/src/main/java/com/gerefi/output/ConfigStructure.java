package com.gerefi.output;

import com.gerefi.ConfigField;

import java.util.List;

public interface ConfigStructure {
    String UNUSED_ANYTHING_PREFIX = "unused";

    ConfigStructure getParent();

    String getName();

    ConfigField getTsFieldByName(String name);

    int getTotalSize();

    List<ConfigField> getTsFields();

    List<ConfigField> getcFields();

    boolean isWithPrefix();

    String getComment();
}
