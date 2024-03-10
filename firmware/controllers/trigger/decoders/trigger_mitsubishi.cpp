/**
 * @file	trigger_mitsubishi.cpp
 *
 * @date Aug 5, 2014
 * @author Andrey Belomutskiy, (c) 2012-2020
 */

#include "pch.h"

#include "trigger_mitsubishi.h"
#include "trigger_universal.h"

void configureFordAspireTriggerWaveform(TriggerWaveform * s) {
	s->initialize(FOUR_STROKE_CAM_SENSOR, SyncEdge::RiseOnly);
	s->isSynchronizationNeeded = false;
	s->isSecondWheelCam = true;

	float x = 121.90;
	float y = 110.86;

	s->addEvent720(x + y - 180, TriggerValue::RISE, TriggerWheel::T_SECONDARY);
	s->addEvent720(x, TriggerValue::FALL, TriggerWheel::T_SECONDARY);
	s->addEvent720(x + y, TriggerValue::RISE, TriggerWheel::T_SECONDARY);
	s->addEvent720(x + 180, TriggerValue::FALL, TriggerWheel::T_SECONDARY);
	s->addEvent720(360, TriggerValue::RISE, TriggerWheel::T_PRIMARY);

	s->addEvent720(x + 180 + y, TriggerValue::RISE, TriggerWheel::T_SECONDARY);
	s->addEvent720(x + 360, TriggerValue::FALL, TriggerWheel::T_SECONDARY);
	s->addEvent720(x + 360 + y, TriggerValue::RISE, TriggerWheel::T_SECONDARY);
	s->addEvent720(x + 540, TriggerValue::FALL, TriggerWheel::T_SECONDARY);
	s->addEvent720(720, TriggerValue::FALL, TriggerWheel::T_PRIMARY);
}

void initializeMitsubishi4gSymmetricalCrank(TriggerWaveform *s) {
		initializeSkippedToothTrigger(s, /* reality */2 * /* symmetrical*/0.5,
				0, FOUR_STROKE_SYMMETRICAL_CRANK_SENSOR, SyncEdge::RiseOnly);
#if EFI_UNIT_TEST
		s->knownOperationMode = true; // this is always placed on crank, overriding 'initializeSkippedToothTrigger' value
#endif
}

// https://github.com/gerefi/gerefi/issues/5593
void initializeVvt6G72(TriggerWaveform *s) {
	s->initialize(FOUR_STROKE_CAM_SENSOR, SyncEdge::Rise);

    s->setTriggerSynchronizationGap(0.8);
    s->setSecondTriggerSynchronizationGap(1.9);
    int narrowWidth = 30;

    // special wider tooth
    s->addToothRiseFall(90 * 1, 60);

    s->addToothRiseFall(90 * 2, narrowWidth);
    s->addToothRiseFall(90 * 3, narrowWidth);
    s->addToothRiseFall(90 * 4, narrowWidth);
}

void initializeMitsubishi4g9xCam(TriggerWaveform *s) {
	s->initialize(FOUR_STROKE_CAM_SENSOR, SyncEdge::Both);

	// nominal gap 0.5
	s->setSecondTriggerSynchronizationGap2(0.2, 0.8);

	// nominal gap 3.0
	s->setTriggerSynchronizationGap2(2.0f, 5.0f);

	// 131 deg before #1 TDC
	// 49 deg after #1 TDC
	s->addToothRiseFall(225, /*width*/90);

	// 131 deg before #4 TDC
	// 41 deg before #4 TDC
	s->addToothRiseFall(360, /*width*/45);
}

void initializeMitsubishi4g63Cam(TriggerWaveform *s) {
	// TODO: is this actually the same as 4G9x or not?
	initializeMitsubishi4g9xCam(s);
}

void initialize36_2_1_1(TriggerWaveform *s) {
	s->initialize(FOUR_STROKE_CRANK_SENSOR, SyncEdge::RiseOnly);
	s->tdcPosition = 90;
	int totalTeethCount = 36;

	float engineCycle = FOUR_STROKE_ENGINE_CYCLE;
	float toothWidth = 0.5;

	float oneTooth = 720 / totalTeethCount;

	float offset = (36 - 11 - 12 - 11) * oneTooth;

	addSkippedToothTriggerEvents(TriggerWheel::T_PRIMARY, s, totalTeethCount, 0, toothWidth, /*offset*/offset, engineCycle,
			NO_LEFT_FILTER, offset + 11 * oneTooth + 1);

	offset += (11 + 1) * oneTooth;

	addSkippedToothTriggerEvents(TriggerWheel::T_PRIMARY, s, totalTeethCount, 0, toothWidth, /*offset*/offset, engineCycle,
			NO_LEFT_FILTER, offset + 11 * oneTooth + 1);


	offset += (11 + 1) * oneTooth;

	addSkippedToothTriggerEvents(TriggerWheel::T_PRIMARY, s, totalTeethCount, 0, toothWidth, /*offset*/offset, engineCycle,
			NO_LEFT_FILTER, offset + 10 * oneTooth + 1);

	s->setTriggerSynchronizationGap(3);
	s->setSecondTriggerSynchronizationGap(1); // redundancy
}

// Mitsubishi 4B11
void initialize36_2_1(TriggerWaveform *s) {
	s->initialize(FOUR_STROKE_CRANK_SENSOR, SyncEdge::RiseOnly);
	s->tdcPosition = 90;
	int totalTeethCount = 36;

	float engineCycle = FOUR_STROKE_ENGINE_CYCLE;
	float toothWidth = 0.5;

	float oneTooth = 720 / totalTeethCount;

	float offset = (36 - 16 - 2 - 17) * oneTooth;

	addSkippedToothTriggerEvents(TriggerWheel::T_PRIMARY, s, totalTeethCount, 0, toothWidth, /*offset*/offset, engineCycle,
			NO_LEFT_FILTER, offset + 16 * oneTooth + 1);

	offset += (16 + 2) * oneTooth;

	addSkippedToothTriggerEvents(TriggerWheel::T_PRIMARY, s, totalTeethCount, 0, toothWidth, /*offset*/offset, engineCycle,
			NO_LEFT_FILTER, offset + 17 * oneTooth + 1);


	s->setTriggerSynchronizationGap(3);
	s->setSecondTriggerSynchronizationGap(1); // redundancy
}

void initializeVvt3A92(TriggerWaveform *s) {
	s->initialize(FOUR_STROKE_CRANK_SENSOR, SyncEdge::RiseOnly);

	int w = 5;
	s->addToothRiseFall(120, w);

	s->addToothRiseFall(12 + 120, w);

	s->addToothRiseFall(240, w);

	s->addToothRiseFall(360, w);

	s->setTriggerSynchronizationGap(9);
	s->setSecondTriggerSynchronizationGap(0.11); // redundancy
}

void initializeMitsubishi4G69Cam(TriggerWaveform *s) {
    s->initialize(FOUR_STROKE_CAM_SENSOR, SyncEdge::RiseOnly);

    int tooth = 5;

    int extraTooth = 20;

	s->addEvent360( 90 - tooth, TriggerValue::RISE, TriggerWheel::T_PRIMARY);
	s->addEvent360( 90, TriggerValue::FALL, TriggerWheel::T_PRIMARY);

	s->addEvent360(180 - extraTooth - tooth, TriggerValue::RISE, TriggerWheel::T_PRIMARY);
	s->addEvent360(180 - extraTooth, TriggerValue::FALL, TriggerWheel::T_PRIMARY);

	s->addEvent360(180 - tooth, TriggerValue::RISE, TriggerWheel::T_PRIMARY);
	s->addEvent360(180, TriggerValue::FALL, TriggerWheel::T_PRIMARY);

	s->addEvent360(270 - extraTooth - tooth, TriggerValue::RISE, TriggerWheel::T_PRIMARY);
	s->addEvent360(270 - extraTooth, TriggerValue::FALL, TriggerWheel::T_PRIMARY);

	s->addEvent360(270 - tooth, TriggerValue::RISE, TriggerWheel::T_PRIMARY);
	s->addEvent360(270, TriggerValue::FALL, TriggerWheel::T_PRIMARY);

	s->addEvent360(360 - tooth, TriggerValue::RISE, TriggerWheel::T_PRIMARY);
	s->addEvent360(360, TriggerValue::FALL, TriggerWheel::T_PRIMARY);

    s->setTriggerSynchronizationGap(1);
   	s->setSecondTriggerSynchronizationGap(4.5);
}
