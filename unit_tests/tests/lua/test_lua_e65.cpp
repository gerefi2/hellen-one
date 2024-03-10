#include "pch.h"
#include "gerefi_lua.h"
#include "lua_lib.h"

#define BMW_CHECKSUM "	function bmwChecksum(canID, data, offset, length) \
		checksum = canID   \
		for i = offset, offset + length - 1,1 \
		do \
	   		checksum = checksum + data[i] \
		end \
		checksum = (checksum >> 8) + (checksum & 0xff) \
		return checksum \
	end "

// https://github.com/HeinrichG-V12/E65_ReverseEngineering/blob/main/docs/0x3B4.md
TEST(LuaE65, Battery) {
	const char* realdata = TWO_BYTES_LSB R"(

	function testFunc()
		data = {0xdc, 0x03, 0x00, 0x53, 0xFE, 0xD3, 0x04, 0x00}
		return getTwoBytesLSB(data, 0, 0.0147)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 14.5236);
}

TEST(LuaE65, testTwoBytes) {
	const char* realdata = TWO_BYTES_LSB R"(

	function testFunc()
		data = {0xdc, 0x03}
		return getTwoBytesLSB(data, 0, 1)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0x03dc);
}

// https://github.com/HeinrichG-V12/E65_ReverseEngineering/blob/main/docs/0x0A8.md
TEST(LuaE65, extractTorqueFromA8) {
	const char* realdata = TWO_BYTES_LSB R"(

function testFunc()
	data = { 0x42, 0x89, 0x10, 0x80, 0x10, 0x0F, 0x00, 0x60 }
	return 0.5 * (getTwoBytesLSB(data, 1, 1) >> 4)
end
)";
			EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0x108 / 2);
}

// http://loopybunny.co.uk/CarPC/can/0AA.html
TEST(LuaE65, Rpm) {
	const char* realdata = TWO_BYTES_LSB R"(

	function testFunc()
		data = {0x5F, 0x59, 0xFF, 0x00, 0x34, 0x0D, 0x80, 0x99}
		return getTwoBytesLSB(data, 4, 0.25)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 845);
}

TEST(LuaE65, gear) {
	const char* realdata = R"(

	function testFunc()
		data = {0x58}
		return data[1] & 0xF
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 8);
}

TEST(LuaE65, repackAA) {
	const char* realdata = ARRAY_EQUALS R"(

	function testFunc()
rpm = 673.75
pedal = 50
		data = {0x58, 12, 14}
		expected = {0x58, 12, 14}
		return equals(data, expected)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0);
}

TEST(LuaE65, gearTorque) {
	const char* realdata = GET_BIT_RANGE_LSB R"(

	function testFunc()
		data = {0x9F, 0x01, 0x32, 0x20, 0x23, 0x30, 0xFF, 0x43}
		return getBitRange(data, 12, 12)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0x320);
}

TEST(LuaE65, offtopicTestGetBitRangeMsb) {
	const char* realdata = GET_BIT_RANGE_MSB R"(

	function testFunc()
		data = {0x9F, 0x41, 0x32, 0x20, 0x23, 0x30, 0xFF, 0x43}
		return getBitRangeMsb(data, 12, 12)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0x9F4);
}

TEST(LuaE65, offtopicTestGetBitRangeMsb2) {
	const char* realdata = GET_BIT_RANGE_MSB R"(

	function testFunc()
		data = {0x70, 0x04, 0x1F}
		return getBitRangeMsb(data, 16, 16)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0x41f);
}

TEST(LuaE65, offtopicTestSetBitRangeMsb2) {
	const char* realdata = PRINT_ARRAY ARRAY_EQUALS SET_BIT_RANGE_MSB R"(

	function testFunc()
		data = {0x68, 0x07}
		expected = {0x68, 0x07}
		setBitRangeMsb(data, 8, 13, 0x807)
		     print(arrayToString(data))
		     print(arrayToString(expected))
		return equals(data, expected)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0);
}

TEST(LuaE65, offtopicTestSetBitRangeMsb3) {
	const char* realdata = PRINT_ARRAY ARRAY_EQUALS SET_BIT_RANGE_MSB R"(

	function testFunc()
		data = {0x68, 0x07}
		expected = {0x63, 0x04}
		setBitRangeMsb(data, 8, 13, 0x304)
		     print(arrayToString(data))
		     print(arrayToString(expected))
		return equals(data, expected)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0);
}

TEST(LuaE65, gearTorque2) {
	const char* realdata = GET_BIT_RANGE_LSB R"(

	function testFunc()
		data = {0x9F, 0x01, 0x32, 0x20, 0x23, 0x30, 0xFF, 0x43}
		return getBitRange(data, 0, 16)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0x019F);
}

TEST(LuaE65, getBitRangeMsbTest) {
	const char* realdata = GET_BIT_RANGE_MSB R"(

	function testFunc()
		data = {0x9F, 0x01, 0x32, 0x20, 0x23, 0x67, 0x40, 0x00}
		return getBitRangeMsb(data, 6 * 8, 13)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0x740);
}

TEST(LuaE65, setBitRangeMsbTest) {
	const char* realdata = PRINT_ARRAY ARRAY_EQUALS SET_BIT_RANGE_MSB R"(

	function testFunc()
				data = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}
		     setBitRangeMsb(data, 6 * 8, 13, 0x740)
    		expected = {0x00, 0x00, 0x00, 0x00, 0x00, 0x07, 0x40, 0x00}
	--	     print(arrayToString(data))
	--	     print(arrayToString(expected))
    		return equals(data, expected)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0);
}

TEST(LuaE65, gearTorque3) {
	const char* realdata = GET_BIT_RANGE_LSB R"(

	function testFunc()
		data = {0x9F, 0xDF, 0x32, 0x20, 0x23, 0x30, 0xFF, 0x43}
		return getBitRange(data, 0, 16)
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0xDF9F);
}


TEST(LuaE65, sumChecksum) {
	// checksum is first byte
	// id = A8, packet:
	// 12 AD 05 A0 05 0F 00 02

	const char* realdata = BMW_CHECKSUM R"(

	function testFunc()
	    canID = 0xA8
		data = { 0xAD, 0x05, 0xA0, 0x05, 0x0F, 0x00, 0x02 }
		checksum = bmwChecksum(canID, data, 1, 7)
		return checksum;
	end)";

	EXPECT_NEAR_M3(testLuaReturnsNumberOrNil(realdata).value_or(0), 0x12);
}
