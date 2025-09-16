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

package com.winterhavenmc.savagegraveyards.models.attributes;

import com.winterhavenmc.savagegraveyards.models.graveyard.attributes.RespawnMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class RespawnMessageTest
{
	@Test
	void of_creates_new_RespawnMessage_with_value()
	{
		// Arrange & Act
		RespawnMessage result = RespawnMessage.of("respawn message");

		// Assert
		assertEquals("respawn message", result.value());
	}


	@Test
	void with_creates_new_RespawnMessage_with_new_value()
	{
		// Arrange
		RespawnMessage respawnMessage = RespawnMessage.of("respawn message");

		// Confirm
		assertEquals("respawn message", respawnMessage.value());

		// Act
		RespawnMessage result = respawnMessage.with("respawn message 2");

		// Assert
		assertEquals("respawn message 2", result.value());
	}


	@Test
	void value_retrieves_RespawnMessage_value()
	{
		// Arrange
		RespawnMessage respawnMessage = RespawnMessage.of("respawn message 3");

		// Act
		String result = respawnMessage.value();

		// Assert
		assertEquals("respawn message 3", result);
	}

}
