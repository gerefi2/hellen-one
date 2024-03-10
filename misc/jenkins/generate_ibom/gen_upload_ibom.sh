#!/bin/bash

echo "Should be executed from project root folder. Will try to upload to $gerefi_SSH_SERVER"
pwd
# ibom is part of Doxygen job simply in order to reduce workspace HDD usage on my tiny build server
bash misc/jenkins/InteractiveHtmlBom/run.sh

if [ -n "$gerefi_SSH_SERVER" ]; then
  echo "Uploading IBOMs"
  cd hardware
  tar -czf - ibom | sshpass -p "$gerefi_SSH_PASS" ssh -o StrictHostKeyChecking=no "$gerefi_SSH_USER"@"$gerefi_SSH_SERVER" "tar -xzf - -C docs"
fi
[ $? -eq 0 ] || { echo "upload FAILED"; exit 1; }
