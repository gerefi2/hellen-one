package com.gerefi.config.generated;

// this file was generated automatically by gerefi tool config_definition_base.jar based on (unknown script) controllers/math/lambda_monitor.txt Sun Jan 07 19:53:44 UTC 2024

// by class com.gerefi.output.FileJavaFieldsConsumer
import com.gerefi.config.*;

public class LambdaMonitor {
	public static final Field LAMBDACURRENTLYGOOD = Field.create("LAMBDACURRENTLYGOOD", 0, FieldType.BIT, 0).setBaseOffset(1448);
	public static final Field LAMBDAMONITORCUT = Field.create("LAMBDAMONITORCUT", 0, FieldType.BIT, 1).setBaseOffset(1448);
	public static final Field LAMBDATIMESINCEGOOD = Field.create("LAMBDATIMESINCEGOOD", 4, FieldType.INT16).setScale(0.01).setBaseOffset(1448);
	public static final Field ALIGNMENTFILL_AT_6 = Field.create("ALIGNMENTFILL_AT_6", 6, FieldType.INT8).setScale(1.0).setBaseOffset(1448);
	public static final Field[] VALUES = {
	LAMBDACURRENTLYGOOD,
	LAMBDAMONITORCUT,
	LAMBDATIMESINCEGOOD,
	ALIGNMENTFILL_AT_6,
	};
}
