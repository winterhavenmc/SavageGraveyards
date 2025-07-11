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

package com.winterhavenmc.savagegraveyards.plugin.models.graveyard.attributes;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.attributes.DiscoveryRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class DiscoveryRangeTest
{
	@Test
	void of_creates_new_DiscoveryRange_with_value()
	{
		// Arrange & Act
		DiscoveryRange result = DiscoveryRange.of(12);

		// Assert
		assertEquals(12, result.value());
	}

	@Test
	void with_creates_new_DiscoveryRange_with_new_value()
	{
		// Arrange
		DiscoveryRange discoveryRange = DiscoveryRange.of(15);

		// Confirm
		assertEquals(15, discoveryRange.value());

		// Act
		DiscoveryRange result = discoveryRange.with(25);

		// Assert
		assertEquals(25, result.value());
	}

	@Test
	void value_retrieves_DiscoveryRange_value()
	{
		// Arrange
		DiscoveryRange discoveryRange = DiscoveryRange.of(30);

		// Act
		int result = discoveryRange.value();

		// Assert
		assertEquals(30, result);
	}

}
