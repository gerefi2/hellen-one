#!/bin/bash

echo This batch files reads gerefi_enums.h and produces auto_generated_enums.* files

#cd ../../../..
#cd ..

BOARD=subaru_eg33

java -DSystemOut.name=logs/gen_enum_to_string \
    -jar ../java_tools/enum_to_string/build/libs/enum_to_string-all.jar \
    -inputPath . \
    -outputPath config/boards/${BOARD}/config/controllers/algo \
    -enumInputFile config/boards/${BOARD}/gerefi_hw_enums.h
