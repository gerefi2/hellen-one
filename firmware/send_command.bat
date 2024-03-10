rem
rem auto-detects connected running gerefi serial port and send text 'reboot' command
rem 

set command=%1
echo "Command: [%command%]"

java -jar ../java_console_binary/gerefi_console.jar send_command %command%
