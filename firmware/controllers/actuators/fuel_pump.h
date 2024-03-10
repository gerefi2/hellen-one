/**
 * @file fuel_pump.h
 */

#pragma once

#include "engine_module.h"
#include "fuel_pump_control_generated.h"
#include <gerefi/timer.h>

class FuelPumpController : public EngineModule, public fuel_pump_control_s {
public:
	void onSlowCallback() override;
	void onIgnitionStateChanged(bool ignitionOn) override;

private:
	Timer m_ignOnTimer;
};
