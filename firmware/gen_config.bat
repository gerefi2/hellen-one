@echo off
bash.exe gen_config.sh

rem This would automatically copy latest file to 'dev' TS project
set ts_path="%HOMEDRIVE%%HOMEPATH%\Documents\TunerStudioProjects"
echo %ts_path%
cp tunerstudio/generated/gerefi.ini %ts_path%\dev\projectCfg\mainController.ini
cp tunerstudio/generated/gerefi_mre_f4.ini %ts_path%\mre_f4\projectCfg\mainController.ini
