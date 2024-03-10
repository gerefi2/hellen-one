#!/bin/bash

echo "This batch files reads gerefi_enums.h and produces auto_generated_enums.* files"

cd ../java_tools
./gradlew :config_definition:shadowJar
./gradlew :enum_to_string:shadowJar
cd ../firmware

rm gen_enum_to_string.log

ENUM_JAR=../java_tools/enum_to_string/build/libs/enum_to_string-all.jar

java -DSystemOut.name=logs/gen_java_enum -cp ${ENUM_JAR} com.gerefi.ToJavaEnum -enumInputFile controllers/sensors/sensor_type.h -outputPath ../java_console/io/src/main/java/com/gerefi/enums
[ $? -eq 0 ] || { echo "ERROR generating sensors"; exit 1; }

java -DSystemOut.name=logs/gen_java_enum -cp ${ENUM_JAR} com.gerefi.ToJavaEnum -enumInputFile controllers/trigger/decoders/sync_edge.h -outputPath ../java_console/io/src/main/java/com/gerefi/enums
[ $? -eq 0 ] || { echo "ERROR generating sensors"; exit 1; }

java -DSystemOut.name=logs/gen_java_enum -cp ${ENUM_JAR} com.gerefi.ToJavaEnum -enumInputFile controllers/algo/engine_types.h   -outputPath ../java_console/models/src/main/java/com/gerefi/enums -definition integration/gerefi_config.txt
[ $? -eq 0 ] || { echo "ERROR generating types"; exit 1; }

java -DSystemOut.name=logs/gen_java_enum \
	-Denum_with_values=true \
	-cp ${ENUM_JAR} com.gerefi.ToJavaEnum \
	-enumInputFile libfirmware/can/can_common.h \
	-outputPath ../java_console/models/src/main/java/com/gerefi/enums \
	-definition libfirmware/can/can_common.h
[ $? -eq 0 ] || { echo "ERROR generating types"; exit 1; }

java -DSystemOut.name=logs/gen_enum_to_string \
	-jar ${ENUM_JAR} \
	-outputPath controllers/algo \
	-generatedFile commonenum \
	-enumInputFile controllers/algo/gerefi_enums.h

[ $? -eq 0 ] || { echo "ERROR generating enums"; exit 1; }

java -DSystemOut.name=logs/gen_enum_to_string \
	-jar ${ENUM_JAR} \
	-outputPath hw_layer/drivers/can \
	-generatedFile can_category \
	-enumInputFile hw_layer/drivers/can/can_category.h

[ $? -eq 0 ] || { echo "ERROR generating enums"; exit 1; }

java -DSystemOut.name=logs/gen_enum_to_string \
	-jar ${ENUM_JAR} \
	-outputPath controllers/trigger/decoders \
	-generatedFile sync_edge \
	-enumInputFile controllers/trigger/decoders/sync_edge.h

[ $? -eq 0 ] || { echo "ERROR generating enums"; exit 1; }

java -DSystemOut.name=logs/gen_enum_to_string \
	-jar ${ENUM_JAR} \
	-outputPath controllers/algo \
	-generatedFile enginetypes \
	-enumInputFile controllers/algo/engine_types.h

[ $? -eq 0 ] || { echo "ERROR generating enums"; exit 1; }

# TODO: rearrange enums so that we have WAY less duplicated generated code? at the moment too many enums are generated 4 times

java -DSystemOut.name=logs/gen_enum_to_string \
	-jar ${ENUM_JAR} \
	-outputPath controllers/algo \
	-enumInputFile controllers/algo/gerefi_hw_enums.h \

[ $? -eq 0 ] || { echo "ERROR generating hw_enums"; exit 1; }

java -DSystemOut.name=logs/gen_enum_to_string \
	-jar ${ENUM_JAR} \
	-outputPath controllers/sensors \
	-generatedFile sensor \
	-enumInputFile controllers/sensors/sensor_type.h

[ $? -eq 0 ] || { echo "ERROR generating sensors"; exit 1; }

pwd
cd config/boards/kinetis/config
./kinetis_gen_enum_to_string.sh
cd ../../../..

cd config/boards/cypress/config
./hellen_cypress_gen_enum_to_string.sh
cd ../../../..

bash config/boards/subaru_eg33/config/gen_enum_to_string.sh
