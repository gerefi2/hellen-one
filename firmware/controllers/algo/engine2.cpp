/*
 * engine2.cpp
 *
 * @date Jan 5, 2019
 * @author Andrey Belomutskiy, (c) 2012-2020
 */

// todo: move this code to more proper locations

#include "pch.h"


#include "speed_density.h"
#include "fuel_math.h"
#include "advance_map.h"
#include "aux_valves.h"
#include "closed_loop_fuel.h"
#include "launch_control.h"
#include "injector_model.h"
#include "tunerstudio.h"

#if EFI_PROD_CODE
#include "svnversion.h"
#endif

#if ! EFI_UNIT_TEST
#include "status_loop.h"
#endif

WarningCodeState::WarningCodeState() {
	clear();
}

void WarningCodeState::clear() {
	warningCounter = 0;
	lastErrorCode = ObdCode::None;
	recentWarnings.clear();
}

void WarningCodeState::addWarningCode(ObdCode code) {
	warningCounter++;
	lastErrorCode = code;

	warning_t* existing = recentWarnings.find(code);

	if (!existing) {
		chibios_rt::CriticalSectionLocker csl;

		// Add the code to the list
		existing = recentWarnings.add(warning_t(code));
	}

	if (existing) {
		// Reset the timer on the code to now
		existing->LastTriggered.reset();
	}

	// Reset the "any warning" timer too
	timeSinceLastWarning.reset();
}

/**
 * @param forIndicator if we want to retrieving value for TS indicator, this case a minimal period is applued
 */
bool WarningCodeState::isWarningNow() const {
	int period = maxI(3, engineConfiguration->warningPeriod);

	return !timeSinceLastWarning.hasElapsedSec(period);
}

// Check whether a particular warning is active
bool WarningCodeState::isWarningNow(ObdCode code) const {
	warning_t* warn = recentWarnings.find(code);

	// No warning found at all
	if (!warn) {
		return false;
	}

	// If the warning is old, it is not active
	return !warn->LastTriggered.hasElapsedSec(maxI(3, engineConfiguration->warningPeriod));
}

EngineState::EngineState() {
	timeSinceLastTChargeK = getTimeNowNt();
}

void EngineState::updateSlowSensors() {
}

void EngineState::periodicFastCallback() {
	ScopePerf perf(PE::EngineStatePeriodicFastCallback);

#if EFI_SHAFT_POSITION_INPUT
	if (!engine->slowCallBackWasInvoked) {
		warning(ObdCode::CUSTOM_SLOW_NOT_INVOKED, "Slow not invoked yet");
	}
	efitick_t nowNt = getTimeNowNt();

	if (engine->rpmCalculator.isCranking()) {
		crankingTimer.reset(nowNt);
	}

	engine->fuelComputer.running.timeSinceCrankingInSecs = crankingTimer.getElapsedSeconds(nowNt);

	recalculateAuxValveTiming();

	int rpm = Sensor::getOrZero(SensorType::Rpm);
	engine->ignitionState.sparkDwell = engine->ignitionState.getSparkDwell(rpm);
	engine->ignitionState.dwellAngle = cisnan(rpm) ? NAN :  engine->ignitionState.sparkDwell / getOneDegreeTimeMs(rpm);

	// todo: move this into slow callback, no reason for IAT corr to be here
	engine->fuelComputer.running.intakeTemperatureCoefficient = getIatFuelCorrection();
	// todo: move this into slow callback, no reason for CLT corr to be here
	engine->fuelComputer.running.coolantTemperatureCoefficient = getCltFuelCorrection();

	engine->module<DfcoController>()->update();
	// should be called before getInjectionMass() and getLimitingTimingRetard()
	getLimpManager()->updateRevLimit(rpm);

	// post-cranking fuel enrichment.
	float m_postCrankingFactor = interpolate3d(
		engineConfiguration->postCrankingFactor,
		engineConfiguration->postCrankingCLTBins, Sensor::getOrZero(SensorType::Clt),
		engineConfiguration->postCrankingDurationBins, engine->rpmCalculator.getRevolutionCounterSinceStart()
	);
	// for compatibility reasons, apply only if the factor is greater than unity (only allow adding fuel)
	// if the engine run time is past the last bin, disable ASE in case the table is filled with values more than 1.0, helps with compatibility
	if ((m_postCrankingFactor < 1.0f) || (engine->rpmCalculator.getRevolutionCounterSinceStart() > engineConfiguration->postCrankingDurationBins[efi::size(engineConfiguration->postCrankingDurationBins)-1])) {
		m_postCrankingFactor = 1.0f;
	}
	engine->fuelComputer.running.postCrankingFuelCorrection = m_postCrankingFactor;

	engine->ignitionState.cltTimingCorrection = getCltTimingCorrection();

	baroCorrection = getBaroCorrection();

	auto tps = Sensor::get(SensorType::Tps1);
	updateTChargeK(rpm, tps.value_or(0));

	float untrimmedInjectionMass = getInjectionMass(rpm) * engine->engineState.lua.fuelMult + engine->engineState.lua.fuelAdd;
	auto clResult = fuelClosedLoopCorrection();

	float fuelLoad = getFuelingLoad();

	injectionStage2Fraction = getStage2InjectionFraction(rpm, fuelLoad);
	float stage2InjectionMass = untrimmedInjectionMass * injectionStage2Fraction;
	float stage1InjectionMass = untrimmedInjectionMass - stage2InjectionMass;

	// Store the pre-wall wetting injection duration for scheduling purposes only, not the actual injection duration
	engine->engineState.injectionDuration = engine->module<InjectorModelPrimary>()->getInjectionDuration(stage1InjectionMass);
	engine->engineState.injectionDurationStage2 =
		engineConfiguration->enableStagedInjection
		? engine->module<InjectorModelSecondary>()->getInjectionDuration(stage2InjectionMass)
		: 0;

	injectionOffset = getInjectionOffset(rpm, fuelLoad);
	engine->lambdaMonitor.update(rpm, fuelLoad);

	float l_ignitionLoad = getIgnitionLoad();
	float baseAdvance = getAdvance(rpm, l_ignitionLoad) * engine->ignitionState.luaTimingMult + engine->ignitionState.luaTimingAdd;
	float correctedIgnitionAdvance = baseAdvance
			// Pull any extra timing for knock retard
			- engine->module<KnockController>()->getKnockRetard()
			// Degrees of timing REMOVED from actual timing during soft RPM limit window
			- getLimpManager()->getLimitingTimingRetard();
	// these fields are scaled_channel so let's only use for observability, with a local variables holding value while it matters locally
	engine->ignitionState.baseIgnitionAdvance = baseAdvance;
	engine->ignitionState.correctedIgnitionAdvance = correctedIgnitionAdvance;


	// compute per-bank fueling
	for (size_t i = 0; i < STFT_BANK_COUNT; i++) {
		float corr = clResult.banks[i];
		engine->stftCorrection[i] = corr;
	}

	// Now apply that to per-cylinder fueling and timing
	for (size_t i = 0; i < engineConfiguration->cylindersCount; i++) {
		uint8_t bankIndex = engineConfiguration->cylinderBankSelect[i];
		auto bankTrim = engine->stftCorrection[bankIndex];
		auto cylinderTrim = getCylinderFuelTrim(i, rpm, fuelLoad);

		// Apply both per-bank and per-cylinder trims
		engine->engineState.injectionMass[i] = untrimmedInjectionMass * bankTrim * cylinderTrim;

		timingAdvance[i] = correctedIgnitionAdvance + getCombinedCylinderIgnitionTrim(i, rpm, l_ignitionLoad);
	}

	shouldUpdateInjectionTiming = getInjectorDutyCycle(rpm) < 90;

	// TODO: calculate me from a table!
	trailingSparkAngle = engineConfiguration->trailingSparkAngle;

	multispark.count = getMultiSparkCount(rpm);

#if EFI_LAUNCH_CONTROL
	engine->launchController.update();
#endif //EFI_LAUNCH_CONTROL

#if EFI_ANTILAG_SYSTEM
	engine->antilagController.update();
#endif //EFI_ANTILAG_SYSTEM
#endif // EFI_SHAFT_POSITION_INPUT
}

#if EFI_ENGINE_CONTROL
void EngineState::updateTChargeK(int rpm, float tps) {
	float newTCharge = engine->fuelComputer.getTCharge(rpm, tps);
	// convert to microsecs and then to seconds
	efitick_t curTime = getTimeNowNt();
	float secsPassed = (float)NT2US(curTime - timeSinceLastTChargeK) / US_PER_SECOND_F;
	if (!cisnan(newTCharge)) {
		// control the rate of change or just fill with the initial value
		sd.tCharge = (sd.tChargeK == 0) ? newTCharge : limitRateOfChange(newTCharge, sd.tCharge, engineConfiguration->tChargeAirIncrLimit, engineConfiguration->tChargeAirDecrLimit, secsPassed);
		sd.tChargeK = convertCelsiusToKelvin(sd.tCharge);
		timeSinceLastTChargeK = curTime;
	}
}
#endif

#if EFI_SIMULATOR
#define VCS_VERSION "123"
#endif

void TriggerConfiguration::update() {
	VerboseTriggerSynchDetails = isVerboseTriggerSynchDetails();
	TriggerType = getType();
}

trigger_config_s PrimaryTriggerConfiguration::getType() const {
	return engineConfiguration->trigger;
}

bool PrimaryTriggerConfiguration::isVerboseTriggerSynchDetails() const {
	return engineConfiguration->verboseTriggerSynchDetails;
}

trigger_config_s VvtTriggerConfiguration::getType() const {
	// Convert from VVT type to trigger_config_s
	return { getVvtTriggerType(engineConfiguration->vvtMode[index]), 0, 0 };
}

bool VvtTriggerConfiguration::isVerboseTriggerSynchDetails() const {
	return engineConfiguration->verboseVVTDecoding;
}

bool isLockedFromUser() {
	int lock = engineConfiguration->tuneHidingKey;
	bool isLocked = lock > 0;
	if (isLocked) {
		criticalError("Tune is password protected. Please use console to unlock tune.");
	}
	return isLocked;
}

void unlockEcu(int password) {
	if (password != engineConfiguration->tuneHidingKey) {
		efiPrintf("Nope rebooting...");
#if EFI_PROD_CODE
		scheduleReboot();
#endif // EFI_PROD_CODE
	} else {
		efiPrintf("Unlocked! Burning...");
		engineConfiguration->tuneHidingKey = 0;
		requestBurn();
	}
}
