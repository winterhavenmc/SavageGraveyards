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

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;


class SafetyTimeTest
{
	@Test
	void of_creates_new_SafetyTime_with_value()
	{
		// Arrange & Act
		SafetyTime result = SafetyTime.of(Duration.ofSeconds(15));

		// Assert
		assertEquals(Duration.ofSeconds(15), result.value());
	}


	@Test
	void with_creates_new_SafetyTime_with_new_value()
	{
		// Arrange
		SafetyTime safetyTime = SafetyTime.of(Duration.ofMinutes(5));

		// Confirm
		assertEquals(Duration.ofMinutes(5), safetyTime.value());

		// Act
		SafetyTime result = safetyTime.with(Duration.ofHours(2));

		// Assert
		assertEquals(Duration.ofHours(2), result.value());
	}


	@Test
	void value_retrieves_SafetyTime_value()
	{
		// Arrange
		SafetyTime safetyTime = SafetyTime.of(Duration.ofSeconds(30));

		// Act
		Duration result = safetyTime.value();

		// Assert
		assertEquals(Duration.ofSeconds(30), result);
	}

}
