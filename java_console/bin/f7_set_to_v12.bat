
cd ../console
java -jar gerefi_console.jar reboot_dfu

sleep 2

cd STM32_Programmer_CLI
cd bin

call stm32-full-erase.bat
STM32_Programmer_CLI.exe -c port=usb1 -w ../../../gerefi.hex -v -s

cd ../..
sleep 2
java -jar gerefi_console.jar send_command "set engine_type 63"