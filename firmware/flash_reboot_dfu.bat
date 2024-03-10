rem
rem auto-detects connected running gerefi serial port and send text 'reboot' command
rem flashes DFU
rem

echo Sending gerefi DFU request
java -jar ../java_console_binary/gerefi_console.jar reboot_dfu
echo Now sleeping before DFU
sleep 5
echo Invoking DFU process
call flash_dfu.bat