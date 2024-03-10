package com.gerefi.ui.config;

import com.opensr5.ConfigurationImage;
import com.gerefi.config.Field;
import com.gerefi.config.FieldCommandResponse;
import com.gerefi.config.FieldType;
import com.gerefi.core.MessagesCentral;
import com.gerefi.core.Pair;
import com.gerefi.ui.UIContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.Map;

public class EnumConfigField extends BaseConfigField {
    private final JComboBox<String> view = new JComboBox<>();
    private boolean ec;
    private final Map<String, Integer> ordinals = new HashMap<>();
    private final String[] options = field.getOptions();

    public EnumConfigField(UIContext uiContext, final Field field, String caption) {
        super(uiContext, field);
        if (options == null)
            throw new NullPointerException("options for " + field);
        createUi(caption, view);

        int ordinal = 0;
        for (String option : options) {
            ordinals.put(option, ordinal++);

            if (!"invalid".equalsIgnoreCase(option))
                view.addItem(option);
        }

        requestInitialValue(field); // this is not in base constructor so that view is created by the time we invoke it

        MessagesCentral.getInstance().addListener(new MessagesCentral.MessageListener() {
            @Override
            public void onMessage(Class clazz, String message) {
                if (FieldCommandResponse.isIntValueMessage(message)) {
                    Pair<Integer, ?> p = FieldCommandResponse.parseResponse(message);
                    if (p != null && p.first == field.getOffset()) {
                        int ordinal = (Integer) p.second;
                        try {
                            setValue(ordinal);
                        } catch (UnexpectedEnumOridnalException e) {
                            System.out.println("ERROR " + e);
                        }
                    }
                }
            }
        });

        view.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ec)
                    return;
                String value = (String) view.getSelectedItem();
                if (value == null)
                    throw new NullPointerException(field.getName());
                int ordinal = ordinals.get(value);
                sendValue(field, Integer.toString(ordinal));
            }
        });
    }

    private void setValue(int ordinal) throws UnexpectedEnumOridnalException {
        String item;
        if (ordinal >= options.length) {
            item = "unexpected_" + ordinal;
            view.addItem(item);
        } else {
            if (ordinal < 0 || ordinal >= options.length)
                throw new UnexpectedEnumOridnalException(ordinal + " unexpected on " + field);
            item = options[ordinal];
        }

        ec = true;
        view.setEnabled(true);
        view.setSelectedItem(item);
        onValueArrived();
        ec = false;
    }

    @Override
    protected void loadValue(ConfigurationImage ci) {
        int ordinal;
        if (field.getType() == FieldType.INT8) {
            ordinal = getByteBuffer(ci).get();
        } else if (field.getType() == FieldType.INT16) {
            ordinal = getByteBuffer(ci).getShort();
        } else {
            ordinal = getByteBuffer(ci).getInt();
        }
        try {
            setValue(ordinal);
        } catch (UnexpectedEnumOridnalException e) {
            log.error("Error loading value " + e);
        }
    }
}