#!/bin/bash

# This batch files reads gerefi_hw_enums.h and produces auto_generated_enums.* files

cd ../../../../..

java -DSystemOut.name=logs/gen_enum_to_string_hellen_cypress \
 -jar ../java_tools/enum_to_string/build/libs/enum_to_string-all.jar \
 -outputPath config/boards/сypress/config/controllers/algo \
 -enumInputFile config/boards/cypress/gerefi_hw_enums.h

