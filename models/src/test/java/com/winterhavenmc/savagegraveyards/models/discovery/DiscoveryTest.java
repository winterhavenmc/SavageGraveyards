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

package com.winterhavenmc.savagegraveyards.models.discovery;

import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.Parameter;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class DiscoveryTest
{
	@Test
	void of_returns_Invalid_given_null_searchKey()
	{
		// Arrange
		UUID playerUid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of((UUID) null, playerUid);

		// Assert
		assertInstanceOf(InvalidDiscovery.class, result);
		assertEquals(FailReason.PARAMETER_NULL, ((InvalidDiscovery) result).discoveryFailReason());
		assertEquals(Parameter.GRAVEYARD_UID, ((InvalidDiscovery) result).parameter());
	}


	@Test
	void of_returns_Invalid_given_null_uid()
	{
		// Arrange
		UUID graveyardUid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(graveyardUid, null);

		// Assert
		assertInstanceOf(InvalidDiscovery.class, result);
		assertEquals(FailReason.PARAMETER_NULL, ((InvalidDiscovery) result).discoveryFailReason());
		assertEquals(Parameter.PLAYER_UID, ((InvalidDiscovery) result).parameter());
	}


	@Test
	void of_returns_Valid_given_valid_searchKey_and_playerUid()
	{
		// Arrange
		UUID graveyardUid = new UUID(86, 86);
		UUID playerUid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(graveyardUid, playerUid);

		// Assert
		assertInstanceOf(ValidDiscovery.class, result);
		assertEquals(new UUID(86, 86), ((ValidDiscovery) result).graveyardUid());
		assertEquals(new UUID(42, 42), ((ValidDiscovery) result).playerUid());
	}


	@Test
	void of_returns_Invalid_given_null_graveyard()
	{
		// Arrange
		UUID playerUid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of((UUID) null, playerUid);

		// Assert
		assertInstanceOf(InvalidDiscovery.class, result);
		assertEquals(FailReason.PARAMETER_NULL, ((InvalidDiscovery) result).discoveryFailReason());
		assertEquals(Parameter.GRAVEYARD_UID, ((InvalidDiscovery) result).parameter());
	}


	@Test
	void of_returns_Invalid_given_null_player()
	{
		// Arrange
		UUID graveyardUid = new UUID(86, 86);

		// Act
		Discovery result = Discovery.of(graveyardUid, null);

		// Assert
		assertInstanceOf(InvalidDiscovery.class, result);
		assertEquals(FailReason.PARAMETER_NULL, ((InvalidDiscovery) result).discoveryFailReason());
		assertEquals(Parameter.PLAYER_UID, ((InvalidDiscovery) result).parameter());
	}


	@Test
	void of_returns_Valid_given_valid_graveyard_and_player()
	{
		// Arrange
		UUID graveyardUid = new UUID(86, 86);
		UUID playerUid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(graveyardUid, playerUid);

		// Assert
		assertInstanceOf(ValidDiscovery.class, result);
	}

}
