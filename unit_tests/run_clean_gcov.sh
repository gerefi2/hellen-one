#!/bin/bash

make clean
make -j$(nproc)
build/gerefi_test
bash ci_gcov.sh "$gerefi_DOXYGEN_FTP_USER" "$gerefi_DOXYGEN_FTP_PASS" "$gerefi_FTP_SERVER"
