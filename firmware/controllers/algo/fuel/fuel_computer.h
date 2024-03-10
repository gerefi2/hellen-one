/**
 * @file fuel_computer.h
 */

#pragma once

class ValueProvider3D;

#include "gerefi_types.h"
#include "fuel_computer_generated.h"

struct IFuelComputer : public fuel_computer_s {
	virtual mass_t getCycleFuel(mass_t airmass, int rpm, float load) = 0;
	temperature_t getTCharge(int rpm, float tps);
	float getLoadOverride(float defaultLoad, load_override_e overrideMode) const;
private:
	float getTChargeCoefficient(int rpm, float tps);
};

// This contains the math of the fuel model, but doesn't actually read any configuration
class FuelComputerBase : public IFuelComputer {
public:
	mass_t getCycleFuel(mass_t airmass, int rpm, float load) override;

	virtual float getStoichiometricRatio() const = 0;
	virtual float getTargetLambda(int rpm, float load) const = 0;
	virtual float getTargetLambdaLoadAxis(float defaultLoad) const = 0;
};

// This class is a usable implementation of a fuel model that reads real configuration
class FuelComputer final : public FuelComputerBase {
public:
	float getStoichiometricRatio() const override;
	float getTargetLambda(int rpm, float load) const override;
	float getTargetLambdaLoadAxis(float defaultLoad) const override;
};

float getLoadOverride(float defaultLoad, load_override_e overrideMode);
constexpr float fuelDensity = 0.72; // g/cc
