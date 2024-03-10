@echo off
cd ../misc/install

[ -e openocd/openocd.exe ] || { echo "openocd/openocd.exe NOT FOUND"; exit 1; }

openocd\openocd.exe -f openocd/stm32f4discovery.cfg  -c "program ../../firmware/build/gerefi.bin verify reset exit 0x08000000"