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

import com.winterhavenmc.savagegraveyards.models.graveyard.ValidGraveyard;
import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;
import org.bukkit.entity.Player;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DiscoveryTest
{
	@Mock Player playerMock;
	@Mock
	ValidGraveyard validGraveyardMock;


	@Test
	void of_returns_Invalid_given_null_searchKey()
	{
		// Arrange
		UUID playerUid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(null, playerUid);

		// Assert
		assertInstanceOf(InvalidDiscovery.class, result);
		assertEquals(DiscoveryFailReason.SEARCH_KEY_NULL, ((InvalidDiscovery) result).discoveryFailReason());
	}


	@Test
	void of_returns_Invalid_given_null_uid()
	{
		// Arrange
		ValidSearchKey searchKey = new ValidSearchKey("Valid_Search_Key");

		// Act
		Discovery result = Discovery.of(searchKey, null);

		// Assert
		assertInstanceOf(InvalidDiscovery.class, result);
		assertEquals(DiscoveryFailReason.PLAYER_UID_NULL, ((InvalidDiscovery) result).discoveryFailReason());
	}


	@Test
	void of_returns_Valid_given_valid_searchKey_and_playerUid()
	{
		// Arrange
		ValidSearchKey searchKey = new ValidSearchKey("Valid_Search_Key");
		UUID uid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(searchKey, uid);

		// Assert
		assertInstanceOf(ValidDiscovery.class, result);
		assertEquals("Valid_Search_Key", ((ValidDiscovery) result).searchKey().string());
		assertEquals(new UUID(42, 42), ((ValidDiscovery) result).playerUid());
	}


	@Test
	void of_returns_Invalid_given_null_graveyard()
	{
		// Arrange
		ValidSearchKey searchKey = new ValidSearchKey("Valid_Search_Key");

		// Act
		Discovery result = Discovery.of(null, playerMock);

		// Assert
		assertInstanceOf(InvalidDiscovery.class, result);
		assertEquals(DiscoveryFailReason.GRAVEYARD_NULL, ((InvalidDiscovery) result).discoveryFailReason());
	}


	@Test
	void of_returns_Invalid_given_null_player()
	{
		// Act
		Discovery result = Discovery.of(validGraveyardMock, null);

		// Assert
		assertInstanceOf(InvalidDiscovery.class, result);
		assertEquals(DiscoveryFailReason.PLAYER_NULL, ((InvalidDiscovery) result).discoveryFailReason());
	}


	@Test
	void of_returns_Valid_given_valid_graveyard_and_player()
	{
		//Arrange
		ValidSearchKey searchKey = new ValidSearchKey("Valid_Search_Key");
		UUID playerUid = new UUID(42, 42);
		when(validGraveyardMock.searchKey()).thenReturn(searchKey);
		when(playerMock.getUniqueId()).thenReturn(playerUid);

		// Act
		Discovery result = Discovery.of(validGraveyardMock, playerMock);

		// Assert
		assertInstanceOf(ValidDiscovery.class, result);
	}

}
