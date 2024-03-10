#include "pch.h"

#include "alternator_controller.h"

using ::testing::StrictMock;
using ::testing::Return;

TEST(Alternator, TestSetPoint) {
	EngineTestHelper eth(engine_type_e::TEST_ENGINE);

	engineConfiguration->targetVBatt = 14.2f;
	engineConfiguration->cranking.rpm = 500;
	engineConfiguration->isAlternatorControlEnabled = true;

	AlternatorController dut;

	Sensor::setMockValue(SensorType::Rpm, 500);
	// disabled if rpm <= cranking.rpm
	EXPECT_EQ(-1, dut.getSetpoint().value_or(-1));

	Sensor::setMockValue(SensorType::Rpm, 501);
	// enabled!
	EXPECT_EQ(engineConfiguration->targetVBatt, dut.getSetpoint().value_or(-1));

	engineConfiguration->isAlternatorControlEnabled = false;
	// disabled manually
	EXPECT_EQ(-1, dut.getSetpoint().value_or(-1));
}

TEST(Alternator, observePlant) {
	EngineTestHelper eth(engine_type_e::TEST_ENGINE);

	AlternatorController dut;

	Sensor::setMockValue(SensorType::BatteryVoltage, 13);
	EXPECT_EQ(13, dut.observePlant().value_or(0));
}

TEST(Alternator, openLoop) {
	AlternatorController dut;

	// No open loop for now
	EXPECT_EQ(dut.getOpenLoop(10), 0);
}

TEST(Alternator, ClosedLoop) {
	EngineTestHelper eth(engine_type_e::TEST_ENGINE);

	AlternatorController dut;
	dut.init();

	engineConfiguration->alternatorControl.pFactor = 1.5f;
	engineConfiguration->alternatorControl.iFactor = 0;
	engineConfiguration->alternatorControl.dFactor = 0;
	engineConfiguration->alternatorControl.offset = 0;	
	// apply PID settings
	dut.pidReset();

	// Target of 30 with position 20 should yield positive duty, P=1.5 means 15% duty for 10% error
	EXPECT_EQ(dut.getClosedLoop(30, 20).value_or(0), 15);
}
