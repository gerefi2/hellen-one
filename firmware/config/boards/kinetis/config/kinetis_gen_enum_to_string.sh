#!/bin/bash

cd ../../../..

java -DSystemOut.name=logs/gen_enum_to_string_kinetis \
 -jar ../java_tools/enum_to_string/build/libs/enum_to_string-all.jar \
 -outputPath config/boards/kinetis/config/controllers/algo \
 -enumInputFile config/boards/kinetis/gerefi_hw_enums.h



