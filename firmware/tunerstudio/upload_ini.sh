#!/usr/bin/env bash

fileName="gerefi_$SHORT_BOARD_NAME.ini"
# user=$1
# pass=$2
# host=$3

if [ ! "$SHORT_BOARD_NAME" ]; then
 echo "No SHORT_BOARD_NAME"
 exit 1
fi

if [ ! "$1" ] || [ ! "$2" ] || [ ! "$3" ]; then
 echo "upload_ini.sh says No Secrets, exiting"
 exit 0
fi

pwd
echo -e "\nUploading all .ini files"
ls -l .

if [ "$fileName" == "no" ]; then
  echo "[upload_ini] signature file not needed"
  exit 0
fi

echo "[upload_ini] Looking for signature in [$fileName]..."
sig=$(grep "^\s*signature\s*=" $fileName         | cut -f2 -d "=")
if [ ! -z "$sig" -a "$sig" != " " ]; then
  echo "* found signature: $sig"
  if [[ "$sig" =~ gerefi\ ([a-zA-Z0-9_-]+)\.([0-9]{4})\.([0-9]{2})\.([0-9]{2})\.([a-zA-Z0-9_-]+)\.([a-zA-Z0-9_-]+) ]]; then
    branch=${BASH_REMATCH[1]}
    year=${BASH_REMATCH[2]}
    month=${BASH_REMATCH[3]}
    day=${BASH_REMATCH[4]}
    board=${BASH_REMATCH[5]}
    hash=${BASH_REMATCH[6]}
    path="$branch/$year/$month/$day/$board/$hash.ini"
    echo "* found path: $path"
    # we do not have ssh for this user
    # sftp does not support -p flag on mkdir :(
    sshpass -p $2 sftp -o StrictHostKeyChecking=no $1@$3 <<SSHCMD
cd gerefi
mkdir $branch
mkdir $branch/$year
mkdir $branch/$year/$month
mkdir $branch/$year/$month/$day
mkdir $branch/$year/$month/$day/$board
put $fileName $path
SSHCMD
    retVal=$?
    if [ $retVal -ne 0 ]; then
      echo "Upload failed"
      exit 1
    fi
    echo "* upload done!"
  else
    echo "[upload_ini] Unexpected Signature: [$sig]"
  fi
else
  echo "Signature not found in $fileName"
  exit 1
fi
