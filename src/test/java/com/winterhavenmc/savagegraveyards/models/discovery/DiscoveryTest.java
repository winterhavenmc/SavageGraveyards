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
		// Arrange & Act
		Discovery result = Discovery.of(1, null);

		// Assert
		assertInstanceOf(Discovery.Invalid.class, result);
		assertEquals("The player UUID was null.", ((Discovery.Invalid) result).reason());
	}


	@Test
	void of_returns_Valid_given_valid_parameters()
	{
		// Arrange
		String searchKey = "search_key";
		UUID uid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(1984, uid);

		// Assert
		assertInstanceOf(Discovery.Valid.class, result);
		assertEquals(1984, ((Discovery.Valid) result).graveyardKey());
		assertEquals(new UUID(42, 42), ((Discovery.Valid) result).playerUid());
	}

}
