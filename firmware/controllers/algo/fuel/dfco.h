/**
 * @file dfco.h
 */

#pragma once
#include "engine_module.h"
#include <gerefi/timer.h>

// DFCO = deceleration fuel cut off, ie, save gas when your foot is off the pedal
class DfcoController : public EngineModule {
public:
	void update();

	bool cutFuel() const;

	float getTimeSinceCut() const;

private:
	bool getState() const;
	bool m_isDfco = false;

	Timer m_timeSinceCut;
	Timer m_timeSinceNoCut;
};
