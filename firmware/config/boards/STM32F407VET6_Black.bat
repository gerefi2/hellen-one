
echo "Compiling for https://gerefi.com/forum/viewtopic.php?f=4&t=1489"
cd ../..
rem TODO: somehow this -DDUMMY is helping us to not mess up the parameters, why?!
rem https://github.com/gerefi/gerefi/issues/684
set EXTRA_PARAMS="-DDUMMY -DEFI_COMMUNICATION_PIN=Gpio::A7"
make -j8 clean

call config/boards/common_make.bat