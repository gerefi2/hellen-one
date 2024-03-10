#pragma once

#include "can_sensor.h"

#include "wideband_state_generated.h"

class AemXSeriesWideband final : public CanSensorBase, public wideband_state_s {
public:
	AemXSeriesWideband(uint8_t sensorIndex, SensorType type);

	bool acceptFrame(const CANRxFrame& frame) const override;

protected:
	// Dispatches to one of the three decoders below
	void decodeFrame(const CANRxFrame& frame, efitick_t nowNt) override;

	// Decode an actual AEM controller, or a gerefi controller sending AEM format
	void decodeAemXSeries(const CANRxFrame& frame, efitick_t nowNt);

	// Decode gerefi custom format
	void decodegerefiStandard(const CANRxFrame& frame, efitick_t nowNt);
	void decodegerefiDiag(const CANRxFrame& frame);

private:
	const uint8_t m_sensorIndex;
};
