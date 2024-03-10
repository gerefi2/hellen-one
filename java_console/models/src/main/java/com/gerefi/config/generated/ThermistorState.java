package com.gerefi.config.generated;

// this file was generated automatically by gerefi tool ConfigDefinition.jar based on integration/thermistor_state.txt Sun Sep 22 08:31:58 EDT 2019

// by class com.gerefi.output.FileJavaFieldsConsumer
import com.gerefi.config.*;

public class ThermistorState {
	public static final Field RESISTANCE = Field.create("RESISTANCE", 0, FieldType.FLOAT);
	public static final Field VOLTAGEMCU = Field.create("VOLTAGEMCU", 4, FieldType.FLOAT);
	public static final Field VOLTAGEBOARD = Field.create("VOLTAGEBOARD", 8, FieldType.FLOAT);
	public static final Field[] VALUES = {
	RESISTANCE,
	VOLTAGEMCU,
	VOLTAGEBOARD,
	};
}
