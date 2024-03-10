package com.opensr5.ini.field;

import com.opensr5.ConfigurationImage;
import com.gerefi.tune.xml.Constant;

import java.util.LinkedList;

public class StringIniField extends IniField {
    private final int size;

    public StringIniField(String name, int offset, int size) {
        super(name, offset);
        this.size = size;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getValue(ConfigurationImage image) {
        String value = new String(image.getContent(), getOffset(), size);
        value = value.trim();
        return value;
    }

    @Override
    public void setValue(ConfigurationImage image, Constant constant) {
        String value = constant.getValue();
        for (int i = 0; i < value.length(); i++)
            image.getContent()[getOffset() + i] = (byte) value.charAt(i);
    }

    public static IniField parse(LinkedList<String> list) {
        String name = list.get(0);
        int offset = Integer.parseInt(list.get(3));
        if (!list.get(2).equalsIgnoreCase("ASCII"))
            throw new IllegalStateException("Do not understand " + name + " at " + offset);
        int size = Integer.parseInt(list.get(4));
        return new StringIniField(name, offset, size);
    }
}
