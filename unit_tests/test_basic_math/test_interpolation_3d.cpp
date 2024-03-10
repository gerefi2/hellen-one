/*
 * @file    test_interpolation_3d.cpp
 *
 *  Created on: Oct 17, 2013
 * @author Andrey Belomutskiy, (c) 2012-2020
 */

#include "pch.h"

#include <stdlib.h>

#include "efi_interpolation.h"

#define RPM_COUNT 5
#define VALUE_COUNT 4

float rpmBins[RPM_COUNT] = { 100, 200, 300, 400, 500 };
scaled_channel<uint8_t, 1, 50> rpmBinsScaledByte[5] = { 100, 200, 300, 400, 500};

float mafBins[VALUE_COUNT] = { 1, 2, 3, 4 };
scaled_channel<int, 10> mafBinsScaledInt[VALUE_COUNT] = { 1, 2, 3, 4 };
scaled_channel<uint8_t, 10> mafBinsScaledByte[VALUE_COUNT] = { 1, 2, 3, 4 };

scaled_channel<uint32_t, 10000, 3> mapScaledChannel[VALUE_COUNT][RPM_COUNT] = {
	{ 1, 2, 3, 4, 4},
	{ 2, 3, 4, 200, 200 },
	{ 3, 4, 200, 500, 500 },
	{ 4, 5, 300, 600, 600 },
};

float map[VALUE_COUNT][RPM_COUNT] = {
	{ 1, 2, 3, 4, 4},
	{ 2, 3, 4, 200, 200 },
	{ 3, 4, 200, 500, 500 },
	{ 4, 5, 300, 600, 600 },
};

static float getValue(float rpm, float maf) {
	Map3D<RPM_COUNT, VALUE_COUNT, float, float, float> x1;
	// note "5, 4" above
	// note "map[4][5], Bins[4], rpm[5] below
	x1.init(map, mafBins, rpmBins);
	float result1 = x1.getValue(rpm, maf);


	Map3D<RPM_COUNT, VALUE_COUNT, float, float, int> x2;
	x2.init(map, mafBinsScaledInt, rpmBins);
	float result2 = x2.getValue(rpm, maf);
	EXPECT_NEAR_M4(result1, result2);


	Map3D<RPM_COUNT, VALUE_COUNT, float, float, uint8_t> x3;
	x3.init(map, mafBinsScaledByte, rpmBins);
	float result3 = x3.getValue(rpm, maf);
	EXPECT_NEAR_M4(result1, result3);

	Map3D<RPM_COUNT, VALUE_COUNT, float, uint8_t, float> x4;
	x4.init(map, mafBins, rpmBinsScaledByte);
	float result4 = x4.getValue(rpm, maf);
	EXPECT_NEAR_M4(result1, result4);

	float result5 = interpolate3d(
		map,
		mafBinsScaledInt, maf,
		rpmBinsScaledByte, rpm
	);
	EXPECT_NEAR_M4(result1, result5);

	// Test with values stored in scaled bytes
	Map3D<RPM_COUNT, VALUE_COUNT, uint32_t, float, float> x6;
	x6.init(mapScaledChannel, mafBins, rpmBins);
	float result6 = x6.getValue(rpm, maf);
	EXPECT_NEAR(result1, result6, 1e-3);

	return result1;
}

static void newTestToComfirmInterpolation() {
// here's how the table loos like:
//
//__RPM_
//__300_|_10|200|
//__200_|__3|__4|
//______|__2|__3|_LOAD

	map[1][2] = 10;
	mapScaledChannel[1][2] = 10;


	// let's start by testing corners
	EXPECT_NEAR_M4(3, getValue(/*rpm*/200, 2));
	EXPECT_NEAR_M4(4, getValue(/*rpm*/200, 3)) << "low rpm high load";

	EXPECT_NEAR_M4(10, getValue(/*rpm*/300, 2));
	EXPECT_NEAR_M4(200, getValue(/*rpm*/300, 3));

	// now testing middles of cell sides
	EXPECT_NEAR_M4(3.5, getValue(/*rpm*/200, 2.5)) << "low rpm middle";
	EXPECT_NEAR_M4(105, getValue(/*rpm*/300, 2.5)) << "high rpm";

	EXPECT_NEAR_M4(6.5, getValue(/*rpm*/250, 2)) << "low load middle";
	EXPECT_NEAR_M4(102, getValue(/*rpm*/250, 3));

	// slowly go from middle side towards center
	EXPECT_NEAR_M4(16.05, getValue(/*rpm*/250, 2.1)) << "middle @ 2.1";
	EXPECT_NEAR_M4(25.6, getValue(/*rpm*/250, 2.2)) << "middle @ 2.2";
	EXPECT_NEAR_M4(35.15, getValue(/*rpm*/250, 2.3)) << "middle @ 2.3";

	EXPECT_NEAR_M4(54.25, getValue(/*rpm*/250, 2.5)) << "middle cell";

	// issue #604: interpolation outside of the table
	// X above the range
	EXPECT_NEAR_M4(230, getValue(/*rpm*/800, 2.1)) << "800 @ 2.1";
	EXPECT_NEAR_M4(290, getValue(/*rpm*/800, 2.3)) << "800 @ 2.3";
	EXPECT_NEAR_M4(530, getValue(/*rpm*/800, 3.3)) << "800 @ 3.3";

	// X below the range
	EXPECT_NEAR_M4(2.1, getValue(/*rpm*/-810, 2.1)) << "-810 @ 2.1";
	EXPECT_NEAR_M4(2.3, getValue(/*rpm*/-820, 2.3)) << "-820 @ 2.3";

	// Y above the range
	EXPECT_NEAR_M4(330, getValue(/*rpm*/310, 12.1)) << "310 @ 12.1";
	EXPECT_NEAR_M4(360, getValue(/*rpm*/320, 12.3)) << "320 @ 12.3";

	// Y below the range
	EXPECT_NEAR_M4(3.1, getValue(/*rpm*/310, -12.1)) << "310 @ -12.1";
	EXPECT_NEAR_M4(3.2, getValue(/*rpm*/320, -12.3)) << "320 @ -12.3";
}

TEST(misc, testInterpolate3d) {
	printf("*** no interpolation here 1\r\n");
	EXPECT_NEAR_M4(2, getValue(100, 2));

	printf("*** no interpolation here 2\r\n");
	EXPECT_NEAR_M4(5, getValue(200, 4));

	printf("*** rpm interpolated value expected1\r\n");
	EXPECT_NEAR_M4(2.5, getValue(150, 2));

	printf("*** rpm interpolated value expected2\r\n");
	EXPECT_NEAR_M4(102, getValue(250, 3));

	printf("*** both rpm and maf interpolated value expected\r\n");
	EXPECT_NEAR_M4(361, getValue(335.3, 3.551));

	printf("*** both rpm and maf interpolated value expected 2\r\n");
	EXPECT_NEAR_M4(203.6, getValue(410.01, 2.012));

	printf("*** both rpm and maf interpolated value expected 3\r\n");
	EXPECT_NEAR_M4(600, getValue(1000000, 1000));

	printf("*** both rpm and maf interpolated value expected 4\r\n");

	EXPECT_NEAR_M4(4, getValue(410.01, -1));


	EXPECT_NEAR_M4(1, getValue(-1, -1));

	newTestToComfirmInterpolation();
}
