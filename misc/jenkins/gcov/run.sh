#!/bin/bash

. /home/jenkins.secrets
echo $gerefi_DOXYGEN_FTP_USER
cd unit_tests
chmod +x run*sh
git submodule update --init
./run_clean_gcov.sh