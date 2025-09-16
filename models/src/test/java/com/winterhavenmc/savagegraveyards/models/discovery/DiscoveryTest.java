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

import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.SearchKey;
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
	Graveyard.Valid validGraveyardMock;


	@Test
	void of_returns_Invalid_given_null_searchKey()
	{
		// Arrange
		UUID playerUid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(null, playerUid);

		// Assert
		assertInstanceOf(Discovery.Invalid.class, result);
		assertEquals(DiscoveryReason.SEARCH_KEY_NULL, ((Discovery.Invalid) result).discoveryReason());
	}


	@Test
	void of_returns_Invalid_given_null_uid()
	{
		// Arrange
		SearchKey.Valid searchKey = new SearchKey.Valid("Valid_Search_Key");

		// Act
		Discovery result = Discovery.of(searchKey, null);

		// Assert
		assertInstanceOf(Discovery.Invalid.class, result);
		assertEquals(DiscoveryReason.PLAYER_UID_NULL, ((Discovery.Invalid) result).discoveryReason());
	}


	@Test
	void of_returns_Valid_given_valid_searchKey_and_playerUid()
	{
		// Arrange
		SearchKey.Valid searchKey = new SearchKey.Valid("Valid_Search_Key");
		UUID uid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(searchKey, uid);

		// Assert
		assertInstanceOf(Discovery.Valid.class, result);
		assertEquals("Valid_Search_Key", ((Discovery.Valid) result).searchKey().string());
		assertEquals(new UUID(42, 42), ((Discovery.Valid) result).playerUid());
	}


	@Test
	void of_returns_Invalid_given_null_graveyard()
	{
		// Arrange
		SearchKey.Valid searchKey = new SearchKey.Valid("Valid_Search_Key");

		// Act
		Discovery result = Discovery.of(null, playerMock);

		// Assert
		assertInstanceOf(Discovery.Invalid.class, result);
		assertEquals(DiscoveryReason.GRAVEYARD_NULL, ((Discovery.Invalid) result).discoveryReason());
	}


	@Test
	void of_returns_Invalid_given_null_player()
	{
		// Act
		Discovery result = Discovery.of(validGraveyardMock, null);

		// Assert
		assertInstanceOf(Discovery.Invalid.class, result);
		assertEquals(DiscoveryReason.PLAYER_NULL, ((Discovery.Invalid) result).discoveryReason());
	}


	@Test
	void of_returns_Valid_given_valid_graveyard_and_player()
	{
		//Arrange
		SearchKey.Valid searchKey = new SearchKey.Valid("Valid_Search_Key");
		UUID playerUid = new UUID(42, 42);
		when(validGraveyardMock.searchKey()).thenReturn(searchKey);
		when(playerMock.getUniqueId()).thenReturn(playerUid);

		// Act
		Discovery result = Discovery.of(validGraveyardMock, playerMock);

		// Assert
		assertInstanceOf(Discovery.Valid.class, result);
	}

}
