package com.gerefi.config.generated;

// this file was generated automatically by gerefi tool config_definition_base.jar based on (unknown script) controllers/engine_cycle/high_pressure_fuel_pump.txt Sun Jan 07 19:53:44 UTC 2024

// by class com.gerefi.output.FileJavaFieldsConsumer
import com.gerefi.config.*;

public class HighPressureFuelPump {
	public static final Field M_REQUESTED_PUMP = Field.create("M_REQUESTED_PUMP", 0, FieldType.FLOAT).setBaseOffset(944);
	public static final Field FUEL_REQUESTED_PERCENT = Field.create("FUEL_REQUESTED_PERCENT", 4, FieldType.FLOAT).setBaseOffset(944);
	public static final Field FUEL_REQUESTED_PERCENT_PI = Field.create("FUEL_REQUESTED_PERCENT_PI", 8, FieldType.FLOAT).setBaseOffset(944);
	public static final Field NOVALVE = Field.create("NOVALVE", 12, FieldType.BIT, 0).setBaseOffset(944);
	public static final Field ANGLEABOVEMIN = Field.create("ANGLEABOVEMIN", 12, FieldType.BIT, 1).setBaseOffset(944);
	public static final Field ISHPFPINACTIVE = Field.create("ISHPFPINACTIVE", 12, FieldType.BIT, 2).setBaseOffset(944);
	public static final Field M_PRESSURETARGET_KPA = Field.create("M_PRESSURETARGET_KPA", 16, FieldType.FLOAT).setBaseOffset(944);
	public static final Field NEXTLOBE = Field.create("NEXTLOBE", 20, FieldType.FLOAT).setBaseOffset(944);
	public static final Field DI_NEXTSTART = Field.create("DI_NEXTSTART", 24, FieldType.FLOAT).setBaseOffset(944);
	public static final Field[] VALUES = {
	M_REQUESTED_PUMP,
	FUEL_REQUESTED_PERCENT,
	FUEL_REQUESTED_PERCENT_PI,
	NOVALVE,
	ANGLEABOVEMIN,
	ISHPFPINACTIVE,
	M_PRESSURETARGET_KPA,
	NEXTLOBE,
	DI_NEXTSTART,
	};
}
