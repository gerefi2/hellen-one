/**
 * @file	gerefi.cpp
 * @brief Initialization code and main status reporting look
 *
 * @date Dec 25, 2013
 * @author Andrey Belomutskiy, (c) 2012-2021
 */

/**
 * @mainpage
 * This documentation https://gerefi.com/docs/html/
 *
 * For version see engine_controller.cpp getgerefiVersion
 *
 * @section sec_intro Intro
 *
 * gerefi is implemented based on the idea that with modern 100+ MHz microprocessors the relatively
 * undemanding task of internal combustion engine control could be implemented in a high-level, processor-independent
 * (to some extent) manner. Thus the key concepts of gerefi: dependency on high-level hardware abstraction layer, software-based PWM etc.
 *
 * @section sec_main Brief overview
 *
 * gerefi runs on crank shaft or cam shaft ('trigger') position sensor events.
 * Once per crank shaft revolution we evaluate the amount of needed fuel and
 * the spark timing. Once we have decided on the parameters for this revolution
 * we schedule all the actions to be triggered by the closest trigger event.
 *
 * We also have some utility threads like idle control thread and communication threads.
 *
 *
 *
 * @section sec_trigger Trigger Decoding
 *
 * Our primary trigger decoder is based on the idea of synchronizing the primary shaft signal and simply counting events on
 * the secondary signal. A typical scenario would be when cam shaft positions sensor is the primary signal and crankshaft is secondary,
 * but sometimes there would be two signals generated by two cam shaft sensors.
 * Another scenario is when we only have crank shaft position sensor, this would make it the primary signal and there would be no secondary signal.
 *
 * There is no software filtering so the signals are expected to be valid. TODO: in reality we are still catching engine stop noise as unrealisticly high RPM.
 *
 * The decoder is configured to act either on the primary signal rise or on the primary signal fall. It then compares the duration
 * of time from the previous signal to the duration of time from the signal before previous, and if the ratio falls into the configurable
 * range between 'syncRatioFrom' and 'syncRatioTo' this is assumed to be the synchronizing event.
 *
 * For instance, for a 36/1 skipped tooth wheel the ratio range for synchronization is from 1.5 to 3
 *
 * Some triggers do not require synchronization, this case we just count signals.
 * A single tooth primary signal would be a typical example when synchronization is not needed.
 *
 *
 * @section sec_timers Timers
 * At the moment gerefi is build using 5 timers:
 * <BR>1) 1MHz microsecond_timer.cpp
 * <BR>2) 10KHz fast ADC callback pwmpcb_fast adc_inputs.cpp
 * <BR>3) slow ADC callback pwmpcb_slow adc_inputs.cpp
 * <BR>4) periodicFastTimer engine_controller.cpp
 * <BR>5) periodicSlowTimer engine_controller.cpp
 *
 *
 *
 * @section sec_scheduler Event Scheduler
 *
 * It is a general agreement to measure all angles in crank shaft angles. In a four stroke
 * engine, a full cycle consists of two revolutions of the crank shaft, so all the angles are
 * running between 0 and 720 degrees.
 *
 * Ignition timing is a great example of a process which highlights the need of a hybrid
 * approach to event scheduling.
 * The most important part of controlling ignition
 * is firing up the spark at the right moment - so, for this job we need 'angle-based' timing,
 * for example we would need to fire up the spark at 700 degrees. Before we can fire up the spark
 * at 700 degrees, we need to charge the ignition coil, for example this dwell time is 4ms - that
 * means we need to turn on the coil at '4 ms before 700 degrees'. Let's  assume that the engine is
 * current at 600 RPM - that means 360 degrees would take 100ms so 4ms is 14.4 degrees at current RPM which
 * means we need to start charting the coil at 685.6 degrees.
 *
 * The position sensors at our disposal are not providing us the current position at any moment of time -
 * all we've got is a set of events which are happening at the knows positions. For instance, let's assume that
 * our sensor sends as an event at 0 degrees, at 90 degrees, at 600 degrees and and 690 degrees.
 *
 * So, for this particular sensor the most precise scheduling would be possible if we schedule coil charting
 * as '85.6 degrees after the 600 degrees position sensor event', and spark firing as
 * '10 degrees after the 690 position sensor event'. Considering current RPM, we calculate that '10 degress after' is
 * 2.777ms, so we schedule spark firing at '2.777ms after the 690 position sensor event', thus combining trigger events
 * with time-based offset.
 *
 * @section tunerstudio Getting Data To and From Tunerstudio
 *
 * Contains the enum with values to be output to Tunerstudio.
 * console/binary/output_channels.txt
 *
 * [Changing gauge limits](http://www.tunerstudio.com/index.php/manuals/63-changing-gauge-limits)
 *
 * Definition of the Tunerstudio configuration interface, gauges, and indicators
 * tunerstudio/gerefi.input
 *
 * @section config Persistent Configuration
 *
 * Definition of configuration data structure:
 * integration/gerefi_config.txt
 * This file has a lot of information and instructions in its comment header.
 * Please note that due to TunerStudio protocol it's important to have the total structure size in sync between the firmware and TS .ini file -
 * just to make sure that this is not forgotten the size of the structure is hard-coded as PAGE_0_SIZE constant. There is always some 'unused' fields added in advance so that
 * one can add some fields without the pain of increasing the total configuration page size.
 * <br>See flash_main.cpp
 *
 * @section sec_fuel_injection Fuel Injection
 *
 *
 * @section sec_misc Misc
 *
 * <BR>See main_trigger_callback.cpp for main trigger event handler
 * <BR>See fuel_math.cpp for details on fuel amount logic
 * <BR>See rpm_calculator.cpp for details on how RPM is calculated
 *
 */

#include "pch.h"

#include "trigger_structure.h"
#include "hardware.h"

#include "rfi_perftest.h"
#include "gerefi.h"
#include "memstreams.h"

#include "eficonsole.h"
#include "status_loop.h"
#include "custom_engine.h"
#include "mpu_util.h"
#include "tunerstudio.h"
#include "mmc_card.h"
#include "mass_storage_init.h"
#include "trigger_emulator_algo.h"
#include "gerefi_lua.h"

#include <setjmp.h>

#if EFI_ENGINE_EMULATOR
#include "engine_emulator.h"
#endif /* EFI_ENGINE_EMULATOR */

bool main_loop_started = false;

static char panicMessage[200];

static virtual_timer_t resetTimer;

// todo: move this into a hw-specific file
void rebootNow() {
	NVIC_SystemReset();
}

/**
 * Some configuration changes require full firmware reset.
 * Once day we will write graceful shutdown, but that would be one day.
 */
void scheduleReboot() {
	efiPrintf("Rebooting in 3 seconds...");
	chibios_rt::CriticalSectionLocker csl;
	chVTSetI(&resetTimer, TIME_MS2I(3000), (vtfunc_t) rebootNow, NULL);
}

static jmp_buf jmpEnv;
void onAssertionFailure() {
	// There's been an assertion failure: instead of hanging, jump back to where we check
	// if (setjmp(jmpEnv)) (see below for more complete explanation)
	longjmp(jmpEnv, 1);
}

void rungerefiWithConfig();
__NO_RETURN void runMainLoop();

void rungerefi() {
	engine->setConfig();

#if EFI_TEXT_LOGGING
	// Initialize logging system early - we can't log until this is called
	startLoggingProcessor();
#endif

#if EFI_PROD_CODE
	checkLastBootError();
#endif

#if defined(STM32F4) || defined(STM32F7)
//	addConsoleAction("stm32_stop", stm32_stop);
	addConsoleAction("stm32_standby", stm32_standby);
#endif

	addConsoleAction(CMD_REBOOT, scheduleReboot);
#if EFI_DFU_JUMP
	addConsoleAction(CMD_REBOOT_DFU, jump_to_bootloader);
#endif /* EFI_DFU_JUMP */

#if EFI_USE_OPENBLT
	addConsoleAction(CMD_REBOOT_OPENBLT, jump_to_openblt);
#endif

	/**
	 * we need to initialize table objects before default configuration can set values
	 */
	initDataStructures();

	// Perform hardware initialization that doesn't need configuration
	initHardwareNoConfig();

  // at the moment that's always hellen board ID
	detectBoardType();

#if EFI_ETHERNET
	startEthernetConsole();
#endif

#if EFI_USB_SERIAL
	startUsbConsole();
#endif

#if HAL_USE_USB_MSD
	initUsbMsd();
#endif

	/**
	 * Next we should initialize serial port console, it's important to know what's going on
	 */
	initializeConsole();

	checkLastResetCause();

	// Read configuration from flash memory
	loadConfiguration();

#if EFI_TUNER_STUDIO
	startTunerStudioConnectivity();
#endif /* EFI_TUNER_STUDIO */

	// Start hardware serial ports (including bluetooth, if present)
#if EFI_TUNER_STUDIO
	startSerialChannels();
#endif // EFI_TUNER_STUDIO

	rungerefiWithConfig();

	// periodic events need to be initialized after fuel&spark pins to avoid a warning
	initPeriodicEvents();

	runMainLoop();
}

void rungerefiWithConfig() {
	// If some config operation caused an OS assertion failure, return immediately
	// This sets the "unwind point" that we can jump back to later with longjmp if we have
	// an assertion failure. If that happens, setjmp() will return non-zero, so we will
	// return immediately from this function instead of trying to init hardware again (which failed last time)
	if (setjmp(jmpEnv)) {
		return;
	}

	commonEarlyInit();


	// Config could be completely bogus - don't start anything else!
	if (validateConfig()) {
		/**
		 * Now let's initialize actual engine control logic
		 * todo: should we initialize some? most? controllers before hardware?
		 */
		initRealHardwareEngineController();


		// This has to happen after RegisteredOutputPins are init'd: otherwise no change will be detected, and no init will happen
		rememberCurrentConfiguration();

	#if EFI_PERF_METRICS
		initTimePerfActions();
	#endif

		runSchedulingPrecisionTestIfNeeded();
	}
}

void runMainLoop() {
	efiPrintf("Running main loop");
	main_loop_started = true;
	/**
	 * This loop is the closes we have to 'main loop' - but here we only publish the status. The main logic of engine
	 * control is around main_trigger_callback
	 */
	while (true) {
#if EFI_CLI_SUPPORT && !EFI_UART_ECHO_TEST_MODE
		// sensor state + all pending messages for our own gerefi console
		// todo: is this mostly dead code?
		updateDevConsoleState();
#endif /* EFI_CLI_SUPPORT */

		chThdSleepMilliseconds(200);
	}
}

/**
 * this depends on chcore.h patch
+void chDbgStackOverflowPanic(thread_t *otp);
+
-    chSysHalt("stack overflow");                                            \
+    chDbgStackOverflowPanic(otp);                                           \

 *
 */
void chDbgStackOverflowPanic(thread_t *otp) {
	(void)otp;
	strcpy(panicMessage, "stack overflow: ");
#if defined(CH_USE_REGISTRY)
	int p_name_len = strlen(otp->p_name);
	if (p_name_len < sizeof(panicMessage) - 2)
		strcat(panicMessage, otp->p_name);
#endif
	chDbgPanic3(panicMessage, __FILE__, __LINE__);
}
