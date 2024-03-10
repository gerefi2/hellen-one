/**
 * @file	event_registry.cpp
 * @brief	This data structure knows when to do what
 *
 * @date Nov 27, 2013
 * @author Andrey Belomutskiy, (c) 2012-2020
 *
 *
 * This file is part of gerefi - see http://gerefi.com
 *
 * gerefi is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * gerefi is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

#include "pch.h"

#include "event_registry.h"

IgnitionEvent::IgnitionEvent() {
	memset(outputs, 0, sizeof(outputs));
}

IgnitionOutputPin * IgnitionEvent::getOutputForLoggins() {
	return outputs[0];
}

