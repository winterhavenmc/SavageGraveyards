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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class DiscoveryMessageTest
{
	@Test
	void of_creates_new_DiscoveryMessage_with_value()
	{
		// Arrange & Act
		DiscoveryMessage result = DiscoveryMessage.of("discovery message");

		// Assert
		assertEquals("discovery message", result.value());
	}


	@Test
	void with_creates_new_DiscoveryMessage_with_new_value()
	{
		// Arrange
		DiscoveryMessage discoveryMessage = DiscoveryMessage.of("discovery message");

		// Confirm
		assertEquals("discovery message", discoveryMessage.value());

		// Act
		DiscoveryMessage result = discoveryMessage.with("discovery message 2");

		// Assert
		assertEquals("discovery message 2", result.value());
	}


	@Test
	void value_retrieves_DiscoveryMessage_value()
	{
		// Arrange
		DiscoveryMessage discoveryMessage = DiscoveryMessage.of("discovery message 3");

		// Act
		String result = discoveryMessage.value();

		// Assert
		assertEquals("discovery message 3", result);
	}

}
