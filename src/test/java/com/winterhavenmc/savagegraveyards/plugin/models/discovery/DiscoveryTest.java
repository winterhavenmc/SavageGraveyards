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

package com.winterhavenmc.savagegraveyards.plugin.models.discovery;

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.SearchKey;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class DiscoveryTest
{
	//TODO: Adapt one of following to test for key less than or equal to zero -> invalid
//	@Test
//	void of_returns_Invalid_given_null_displayName()
//	{
//		// Arrange
//		UUID uid = new UUID(42, 42);
//
//		// Act
//		Discovery result = Discovery.of(null, uid);
//
//		// Assert
//		assertInstanceOf(Discovery.Invalid.class, result);
//		assertEquals("The search key was null.", ((Discovery.Invalid) result).reason());
//	}

//	@Test
//	void of_returns_Invalid_given_blank_displayName()
//	{
//		// Arrange
//		UUID uid = new UUID(42, 42);
//
//		// Act
//		Discovery result = Discovery.of("", uid);
//
//		// Assert
//		assertInstanceOf(Discovery.Invalid.class, result);
//		assertEquals("The search key was blank.", ((Discovery.Invalid) result).reason());
//	}


	@Test
	void of_returns_Invalid_given_null_uid()
	{
		// Arrange
		SearchKey.Valid searchKey = new SearchKey.Valid("Valid_Search_Key");

		// Act
		Discovery result = Discovery.of(searchKey, null);

		// Assert
		assertInstanceOf(Discovery.Invalid.class, result);
		assertEquals("The player UUID was null.", ((Discovery.Invalid) result).reason());
	}


	@Test
	void of_returns_Valid_given_valid_parameters()
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

}
