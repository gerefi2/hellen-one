rem Depends on ST Link and ST VCP being installed manually

rm -rf silent_st_drivers/ST-LINK_USB_V2_1_Driver
rm -rf silent_st_drivers/"Virtual comport driver"
rem Just safer not to have the folder at all
rm -rf silent_st_drivers/DFU_Driver

cp -r "C:\Program Files (x86)\STMicroelectronics\STM32 ST-LINK Utility\ST-LINK_USB_V2_1_Driver" silent_st_drivers
rem https://github.com/gerefi/gerefi_external_utils/blob/master/stsw-stm32102_1_4_0.zip
cp -r "C:\Program Files (x86)\STMicroelectronics\Software\Virtual comport driver" silent_st_drivers

cp -r "C:\Program Files\STMicroelectronics\STM32Cube\STM32CubeProgrammer\Drivers\DFU_Driver" silent_st_drivers
cp install_elevated_STM32Bootloader.bat silent_st_drivers\DFU_Driver

rem compress 'silent_st_drivers' folder
"C:\Program Files\7-Zip\7z.exe" a silent_st_drivers2.exe -mmt -mx5 -sfx silent_st_drivers