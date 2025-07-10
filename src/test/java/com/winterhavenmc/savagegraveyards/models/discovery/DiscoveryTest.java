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
	@Test
	void of_returns_Invalid_given_null_displayName()
	{
		// Arrange
		UUID uid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(null, uid);

		// Assert
		assertInstanceOf(Discovery.Invalid.class, result);
		assertEquals("The search key was null.", ((Discovery.Invalid) result).reason());
	}

	@Test
	void of_returns_Invalid_given_blank_displayName()
	{
		// Arrange
		UUID uid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of("", uid);

		// Assert
		assertInstanceOf(Discovery.Invalid.class, result);
		assertEquals("The search key was blank.", ((Discovery.Invalid) result).reason());
	}


	@Test
	void of_returns_Invalid_given_null_uid()
	{
		// Arrange
		String name = "display name";

		// Act
		Discovery result = Discovery.of(name, null);

		// Assert
		assertInstanceOf(Discovery.Invalid.class, result);
		assertEquals("The player UUID was null.", ((Discovery.Invalid) result).reason());
	}


	@Test
	void of_returns_Valid_given_valid_parameters()
	{
		// Arrange
		String name = "display name";
		UUID uid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(name, uid);

		// Assert
		assertInstanceOf(Discovery.Valid.class, result);
		assertEquals("display name", result.displayName());
		assertEquals(new UUID(42, 42), ((Discovery.Valid) result).playerUid());
	}


	@Test
	void searchKey_returns_valid_searchKey()
	{
		// Arrange
		String name = "Display &aName&r";
		UUID uid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(name, uid);

		// Assert
		assertInstanceOf(Discovery.Valid.class, result);
		assertEquals("Display_Name", result.searchKey());
	}


	@Test
	void displayName_returns_valid_displayName()
	{
		// Arrange
		String name = "Display &aName&r";
		UUID uid = new UUID(42, 42);

		// Act
		Discovery result = Discovery.of(name, uid);

		// Assert
		assertInstanceOf(Discovery.Valid.class, result);
		assertEquals("Display &aName&r", result.displayName());
	}

}