/*
 * Copyright (c) 2025 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.savagegraveyards.models.graveyard.attributes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class SafetyRangeTest
{
	@Test
	void of_creates_new_SafetyRange_with_value()
	{
		// Arrange & Act
		SafetyRange result = SafetyRange.of(12);

		// Assert
		assertEquals(12, result.value());
	}

	@Test
	void with_creates_new_SafetyRange_with_new_value()
	{
		// Arrange
		SafetyRange safetyRange = SafetyRange.of(15);

		// Confirm
		assertEquals(15, safetyRange.value());

		// Act
		SafetyRange result = safetyRange.with(25);

		// Assert
		assertEquals(25, result.value());
	}

	@Test
	void value_retrieves_SafetyRange_value()
	{
		// Arrange
		SafetyRange safetyRange = SafetyRange.of(30);

		// Act
		int result = safetyRange.value();

		// Assert
		assertEquals(30, result);
	}

}
