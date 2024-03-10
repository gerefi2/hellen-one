/**
 * @file	engine_emulator.cpp
 * @brief	Entry point for all the emulation and analysis code
 *
 * there is a chance that 90% of the code here is dead
 *
 * @date Mar 15, 2013
 * @author Andrey Belomutskiy, (c) 2012-2020
 */

#include "pch.h"
#include "engine_emulator.h"

#include "poten.h"
#include "trigger_emulator_algo.h"

void initEngineEmulator() {
	if (hasFirmwareError())
		return;

#if EFI_POTENTIOMETER && HAL_USE_SPI
	initPotentiometers();
#endif /* EFI_POTENTIOMETER && HAL_USE_SPI*/

#if EFI_EMULATE_POSITION_SENSORS
	initTriggerEmulator();
#endif // EFI_EMULATE_POSITION_SENSORS
}
