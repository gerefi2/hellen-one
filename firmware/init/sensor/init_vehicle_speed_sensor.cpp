#include "pch.h"

#include "init.h"
#include "frequency_sensor.h"
#include "vehicle_speed_converter.h"

// 0.05 filter parameter means averaging over ~20 sensor teeth
FrequencySensor vehicleSpeedSensor(SensorType::VehicleSpeed, MS2NT(500));
static VehicleSpeedConverter vehicleSpeedConverter;

void initVehicleSpeedSensor() {
	int parameter = engineConfiguration->vssFilterReciprocal;

	if (parameter < VSS_FILTER_MIN || parameter > VSS_FILTER_MAX) {
		parameter = VSS_FILTER_MIN;
	}

	float filterParameter = 1.0f / parameter;

	vehicleSpeedSensor.initIfValid(engineConfiguration->vehicleSpeedSensorInputPin, vehicleSpeedConverter, filterParameter);
}

void deInitVehicleSpeedSensor() {
	vehicleSpeedSensor.deInit();
}
