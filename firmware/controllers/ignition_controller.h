#pragma once

#include "engine_module.h"

#include <gerefi/timer.h>

class IgnitionController : public EngineModule {
public:
	void onSlowCallback() override;

private:
	Timer m_timeSinceIgnVoltage;
	bool m_lastState = false;
};
