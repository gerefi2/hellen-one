/*
 * @file	mazda_miata_vvt.h
 *
 * @date Oct 4, 2016
 * @author Andrey Belomutskiy, (c) 2012-2020
 *
 * http://gerefi.com/forum/viewtopic.php?f=3&t=1095
 */

#pragma once

#include "engine_configuration.h"

/**
 * Primary gerefi test mule https://gerefi.com/forum/viewtopic.php?f=3&t=1095
 * MAZDA_MIATA_2003
 * set engine_type 47
 */
void setMazdaMiata2003EngineConfiguration();

/**
 * https://github.com/gerefi/gerefi/wiki/Mazda-Miata-2001
 * set engine_type 1
 */
void setMiataNB2_Proteus_TCU();

/**
 * set engine_type 67
 */
void setMiataNB2_Proteus();

/**
 * set engine_type 69
 */
void setMiataNB2_Hellen72();

void setMiataNB2_Hellen72_36();


void setHellenNB1();
