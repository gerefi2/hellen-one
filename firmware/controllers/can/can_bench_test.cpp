
#include "pch.h"
#include "bench_test.h"
#include "board_id.h"
#include "can_bench_test.h"
#include "can_msg_tx.h"
#include "can_common.h"
#include "frequency_sensor.h"
#include "settings.h"
#include "gpio/gpio_ext.h"

#define TRUNCATE_TO_BYTE(i) ((i) & 0xff)
// raw values are 0..5V, convert it to 8-bit (0..255)
#define RAW_TO_BYTE(v) TRUNCATE_TO_BYTE((int)(v * 255.0 / 5.0))

/**
 * QC direct output control API is used by https://github.com/gerefi/stim test device
 * quite different from bench testing user functionality: QC direct should never be engaged on a real vehicle
 * Once QC direct control mode is activated the only way out is to reboot the unit!
 */
bool qcDirectPinControlMode = false;

static void directWritePad(Gpio pin, int value) {
#if EFI_GPIO_HARDWARE && EFI_PROD_CODE
	if (brain_pin_is_onchip(pin)) {
	  palWritePad(getHwPort("can_write", pin), getHwPin("can_write", pin), value);
	} else {
#if (BOARD_EXT_GPIOCHIPS > 0)
  	gpiochips_writePad(pin, value);
#endif
	}
#endif // EFI_GPIO_HARDWARE && EFI_PROD_CODE
}

static void qcSetEtbState(uint8_t dcIndex, uint8_t direction) {
	qcDirectPinControlMode = true;
	const dc_io *io = &engineConfiguration->etbIo[dcIndex];
	Gpio controlPin = io->controlPin;
	directWritePad(controlPin, 1);
	efiSetPadModeWithoutOwnershipAcquisition("QC_ETB", controlPin, PAL_MODE_OUTPUT_PUSHPULL);
	directWritePad(io->directionPin1, direction);
	directWritePad(io->disablePin, 0); // disable pin is inverted - here we ENABLE. direct pin access due to qcDirectPinControlMode
}

#if EFI_CAN_SUPPORT

static void setPin(const CANRxFrame& frame, int value) {
		int outputIndex = frame.data8[2];
		if (outputIndex >= getBoardMetaOutputsCount()) {
		  criticalError("QC pin index %d", outputIndex);
			return;
	  }
	  Gpio* boardOutputs = getBoardMetaOutputs();
	  criticalAssertVoid(boardOutputs != nullptr, "outputs not defined");
		Gpio pin = boardOutputs[outputIndex];
#if EFI_GPIO_HARDWARE && EFI_PROD_CODE

        int hwIndex = brainPin_to_index(pin);
        if (engine->pinRepository.getBrainUsedPin(hwIndex) == nullptr) {
            // if pin is assigned we better configure it
            efiSetPadModeWithoutOwnershipAcquisition("QC_SET", pin, PAL_MODE_OUTPUT_PUSHPULL);
        }

        directWritePad(pin, value);
#endif // EFI_GPIO_HARDWARE && EFI_PROD_CODE
}

void sendQcBenchEventCounters() {
#if EFI_SHAFT_POSITION_INPUT
	CanTxMessage msg(CanCategory::BENCH_TEST, (int)bench_test_packet_ids_e::EVENT_COUNTERS, 8, /*bus*/0, /*isExtended*/true);

	int primaryFall = engine->triggerCentral.getHwEventCounter((int)SHAFT_PRIMARY_FALLING);
	int primaryRise = engine->triggerCentral.getHwEventCounter((int)SHAFT_PRIMARY_RISING);
	int secondaryFall = engine->triggerCentral.getHwEventCounter((int)SHAFT_SECONDARY_FALLING);
	int secondaryRise = engine->triggerCentral.getHwEventCounter((int)SHAFT_SECONDARY_RISING);

	msg[0] = TRUNCATE_TO_BYTE(primaryRise + primaryFall);
	msg[1] = TRUNCATE_TO_BYTE(secondaryRise + secondaryFall);

	for (int camIdx = 0; camIdx < 4; camIdx++) {
		int vvtRise = 0, vvtFall = 0;
		if (camIdx < CAM_INPUTS_COUNT) {
			vvtRise = engine->triggerCentral.vvtEventRiseCounter[camIdx];
			vvtFall = engine->triggerCentral.vvtEventFallCounter[camIdx];
		}

		msg[2 + camIdx] = TRUNCATE_TO_BYTE(vvtRise + vvtFall);
	}

	extern FrequencySensor vehicleSpeedSensor;
	msg[6] = TRUNCATE_TO_BYTE(vehicleSpeedSensor.eventCounter);
#endif // EFI_SHAFT_POSITION_INPUT
}

void sendQcBenchButtonCounters() {
	CanTxMessage msg(CanCategory::BENCH_TEST, (int)bench_test_packet_ids_e::BUTTON_COUNTERS, 8, /*bus*/0, /*isExtended*/true);
	msg[0] = TRUNCATE_TO_BYTE(engine->brakePedalSwitchedState.getCounter());
	msg[1] = TRUNCATE_TO_BYTE(engine->clutchUpSwitchedState.getCounter());
	msg[2] = TRUNCATE_TO_BYTE(engine->acButtonSwitchedState.getCounter());
	// todo: start button
}

void sendQcBenchAuxDigitalCounters() {
	CanTxMessage msg(CanCategory::BENCH_TEST, (int)bench_test_packet_ids_e::BUTTON_COUNTERS, 8, /*bus*/0, /*isExtended*/true);
  for (int i =0;i<LUA_DIGITAL_INPUT_COUNT;i++) {
	  msg[i] = TRUNCATE_TO_BYTE(engine->luaDigitalInputState[i].state.getCounter());
  }
}

void sendQcBenchRawAnalogValues() {
	const float values_1[] = {
		Sensor::getRaw(SensorType::Tps1Primary),
		Sensor::getRaw(SensorType::Tps1Secondary),
		Sensor::getRaw(SensorType::AcceleratorPedalPrimary),
		Sensor::getRaw(SensorType::AcceleratorPedalSecondary),
		Sensor::getRaw(SensorType::MapSlow),
		Sensor::getRaw(SensorType::Clt),
		Sensor::getRaw(SensorType::Iat),
		Sensor::getRaw(SensorType::BatteryVoltage),
	};

	const float values_2[] = {
		Sensor::getRaw(SensorType::Tps2Primary),
		Sensor::getRaw(SensorType::Tps2Secondary),
		Sensor::getRaw(SensorType::AuxLinear1),
		Sensor::getRaw(SensorType::AuxLinear2),
		Sensor::getRaw(SensorType::OilPressure),
		Sensor::getRaw(SensorType::FuelPressureLow),
		Sensor::getRaw(SensorType::FuelPressureHigh),
		Sensor::getRaw(SensorType::AuxTemp1),
	};
	static_assert(efi::size(values_1) <= 8);
	static_assert(efi::size(values_2) <= 8);


	// send the first packet
	{
		CanTxMessage msg(CanCategory::BENCH_TEST, (int)bench_test_packet_ids_e::RAW_ANALOG_1, 8, /*bus*/0, /*isExtended*/true);
		for (size_t valueIdx = 0; valueIdx < efi::size(values_1); valueIdx++) {
			msg[valueIdx] = RAW_TO_BYTE(values_1[valueIdx]);
		}
	}
	{
		CanTxMessage msg(CanCategory::BENCH_TEST, (int)bench_test_packet_ids_e::RAW_ANALOG_2, 8, /*bus*/0, /*isExtended*/true);
		for (size_t valueIdx = 0; valueIdx < efi::size(values_2); valueIdx++) {
			msg[valueIdx] = RAW_TO_BYTE(values_2[valueIdx]);
		}
	}
}

static void sendOutBoardMeta() {
#if EFI_PROD_CODE
	CanTxMessage msg(CanCategory::BENCH_TEST, (int)bench_test_packet_ids_e::IO_META_INFO, 8, /*bus*/0, /*isExtended*/true);
	msg[0] = (int)bench_test_magic_numbers_e::BENCH_HEADER;
	msg[1] = 0;
	msg[2] = getBoardMetaOutputsCount();
	msg[3] = getBoardMetaLowSideOutputsCount();
	msg[4] = getBoardMetaDcOutputsCount();
#endif // EFI_PROD_CODE
}

void sendQcBenchBoardStatus() {
#if EFI_PROD_CODE
	CanTxMessage msg(CanCategory::BENCH_TEST, (int)bench_test_packet_ids_e::BOARD_STATUS, 8, /*bus*/0, /*isExtended*/true);

	int boardId = getBoardId();
	msg[0] = TRUNCATE_TO_BYTE(boardId >> 8);
	msg[1] = TRUNCATE_TO_BYTE(boardId);

	int numSecondsSinceReset = getTimeNowS();
	msg[2] = TRUNCATE_TO_BYTE(numSecondsSinceReset >> 16);
	msg[3] = TRUNCATE_TO_BYTE(numSecondsSinceReset >> 8);
	msg[4] = TRUNCATE_TO_BYTE(numSecondsSinceReset);

    int engineType = (int) engineConfiguration->engineType;
	msg[5] = engineType >> 8;
	msg[6] = engineType;
	sendOutBoardMeta();
#endif // EFI_PROD_CODE
}

static void sendPinStatePackets(int pinToggleCounter, uint32_t durationsInStateMs[2]) {
	CanTxMessage msg(CanCategory::BENCH_TEST, (int)bench_test_packet_ids_e::PIN_STATE, 8, /*bus*/0, /*isExtended*/true);
	msg[0] = TRUNCATE_TO_BYTE(pinToggleCounter >> 8);
	msg[1] = TRUNCATE_TO_BYTE(pinToggleCounter);

	for (int i = 0, mIdx = 2; i < 2; i++) {
		msg[mIdx++] = TRUNCATE_TO_BYTE(durationsInStateMs[i] >> 16);
		msg[mIdx++] = TRUNCATE_TO_BYTE(durationsInStateMs[i] >> 8);
		msg[mIdx++] = TRUNCATE_TO_BYTE(durationsInStateMs[i]);
	}
}

// bench test fuel pump pin #5603
static void sendPinStatePackets(bench_mode_e benchModePinIdx) {
    OutputPin *pin = enginePins.getOutputPinForBenchMode(benchModePinIdx);
    if (pin == nullptr)
    	return;
#if EFI_SIMULATOR
	sendPinStatePackets(pin->pinToggleCounter, pin->durationsInStateMs);
#endif // EFI_SIMULATOR
}

static void sendSavedBenchStatePackets() {
	uint32_t savedDurationsInStateMs[2];
	int savedPinToggleCounter = getSavedBenchTestPinStates(savedDurationsInStateMs);
	sendPinStatePackets(savedPinToggleCounter, savedDurationsInStateMs);
}

static void resetPinStats(bench_mode_e benchModePinIdx) {
    OutputPin *pin = enginePins.getOutputPinForBenchMode(benchModePinIdx);

    if (pin == nullptr)
    	return;

#if EFI_SIMULATOR
	pin->resetToggleStats();
#endif // EFI_SIMULATOR
}

void processCanQcBenchTest(const CANRxFrame& frame) {
	if (CAN_EID(frame) != (int)bench_test_packet_ids_e::IO_CONTROL) {
		return;
	}
	if (frame.data8[0] != (int)bench_test_magic_numbers_e::BENCH_HEADER) {
		return;
	}
	bench_test_io_control_e command = (bench_test_io_control_e)frame.data8[1];
	if (command == bench_test_io_control_e::CAN_BENCH_GET_COUNT) {
	    sendOutBoardMeta();
	} else if (command == bench_test_io_control_e::CAN_QC_OUTPUT_CONTROL_SET) {
		qcDirectPinControlMode = true;
	    setPin(frame, 1);
	} else if (command == bench_test_io_control_e::CAN_QC_OUTPUT_CONTROL_CLEAR) {
		qcDirectPinControlMode = true;
	    setPin(frame, 0);
	} else if (command == bench_test_io_control_e::CAN_QC_ETB) {
		uint8_t dcIndex = frame.data8[2];
		uint8_t direction = frame.data8[3];
		qcSetEtbState(dcIndex, direction);
	} else if (command == bench_test_io_control_e::CAN_BENCH_SET_ENGINE_TYPE) {
		int eType = frame.data8[2];
		// todo: fix firmware for 'false' to be possible - i.e. more of properties should be applied on the fly
		setEngineType(eType, true);
#if EFI_PROD_CODE
		scheduleReboot();
#endif // EFI_PROD_CODE
} else if (command == bench_test_io_control_e::CAN_BENCH_START_PIN_TEST) {
		bench_mode_e benchModePinIdx = (bench_mode_e)frame.data8[2];
		// ignore previous pin state and stats
		resetPinStats(benchModePinIdx);
	} else if (command == bench_test_io_control_e::CAN_BENCH_END_PIN_TEST) {
		sendSavedBenchStatePackets();
	} else if (command == bench_test_io_control_e::CAN_BENCH_EXECUTE_BENCH_TEST) {
		int benchCommandIdx = frame.data8[2];
		handleBenchCategory(benchCommandIdx);
	} else if (command == bench_test_io_control_e::CAN_BENCH_QUERY_PIN_STATE) {
		bench_mode_e benchModePinIdx = (bench_mode_e)frame.data8[2];
		sendPinStatePackets(benchModePinIdx);
	}
}
#endif // EFI_CAN_SUPPORT

void initQcBenchControls() {
    addConsoleActionII("qc_etb", [](int index, int direction) {
        qcSetEtbState(index, direction);
    });
}
