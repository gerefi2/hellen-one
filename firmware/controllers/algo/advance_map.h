/*
 * @file advance_map.h
 *
 * @date Mar 27, 2013
 * @author Andrey Belomutskiy, (c) 2012-2020
 */

#include "ignition_state_generated.h"

#pragma once

angle_t getAdvance(int rpm, float engineLoad);
angle_t getCombinedCylinderIgnitionTrim(size_t cylinderNumber, int rpm, float ignitionLoad);
/**
 * this method is used to build default advance map
 */
float getInitialAdvance(int rpm, float map, float advanceMax);

size_t getMultiSparkCount(int rpm);
void initIgnitionAdvanceControl();

class IgnitionState : public ignition_state_s {
public:
	floatms_t getSparkDwell(int rpm);
};
