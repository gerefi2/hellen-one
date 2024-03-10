package com.gerefi.config.generated;

// this file was generated automatically by gerefi tool config_definition_base.jar based on (unknown script) controllers/actuators/fuel_pump_control.txt Sun Jan 07 19:53:44 UTC 2024

// by class com.gerefi.output.FileJavaFieldsConsumer
import com.gerefi.config.*;

public class FuelPump {
	public static final Field ISPRIME = Field.create("ISPRIME", 0, FieldType.BIT, 0).setBaseOffset(1048);
	public static final Field ENGINETURNEDRECENTLY = Field.create("ENGINETURNEDRECENTLY", 0, FieldType.BIT, 1).setBaseOffset(1048);
	public static final Field ISFUELPUMPON = Field.create("ISFUELPUMPON", 0, FieldType.BIT, 2).setBaseOffset(1048);
	public static final Field IGNITIONON = Field.create("IGNITIONON", 0, FieldType.BIT, 3).setBaseOffset(1048);
	public static final Field[] VALUES = {
	ISPRIME,
	ENGINETURNEDRECENTLY,
	ISFUELPUMPON,
	IGNITIONON,
	};
}
