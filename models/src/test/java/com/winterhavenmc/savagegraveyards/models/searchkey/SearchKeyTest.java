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

package com.winterhavenmc.savagegraveyards.models.searchkey;

import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.displayname.DisplayName;
import com.winterhavenmc.savagegraveyards.models.displayname.ValidDisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class SearchKeyTest
{
	@Test @org.junit.jupiter.api.DisplayName("Of returns SearchKey.Invalid type given null string.")
	void of_returns_Invalid_given_null_string()
	{
		// Act
		SearchKey result = SearchKey.of((String) null);

		// Assert
		assertInstanceOf(InvalidSearchKey.class, result);
		assertEquals("∅", result.string());
		assertEquals(FailReason.PARAMETER_NULL, ((InvalidSearchKey) result).reason());
	}


	@Test @org.junit.jupiter.api.DisplayName("Of returns SearchKey.Invalid type given blank string.")
	void of_returns_Invalid_given_blank_string()
	{
		// Act
		SearchKey result = SearchKey.of("");

		// Assert
		assertInstanceOf(InvalidSearchKey.class, result);
		assertEquals("⬚", result.string());
		assertEquals(FailReason.PARAMETER_BLANK, ((InvalidSearchKey) result).reason());
	}


	@Test @org.junit.jupiter.api.DisplayName("Of returns SearchKey.Valid type given valid parameters.")
	void of_returns_Valid_given_valid_parameters()
	{
		// Act
		SearchKey result = SearchKey.of("Valid Search Key");

		// Assert
		assertInstanceOf(ValidSearchKey.class, result);
	}


	@Test @org.junit.jupiter.api.DisplayName("Of returns SearchKey.Valid type given valid parameters.")
	void of_returns_Valid_given_valid_List_parameter()
	{
		// Act
		SearchKey result = SearchKey.of(List.of("Valid", "Search", "Key"));

		// Assert
		assertInstanceOf(ValidSearchKey.class, result);
		assertEquals("Valid_Search_Key", result.string());
	}


	@Test @org.junit.jupiter.api.DisplayName("string() method returns valid searchKey string.")
	void string()
	{
		// Act
		SearchKey result = SearchKey.of("Valid Search Key");

		// Assert
		assertEquals("Valid_Search_Key", result.string());
	}


	@Test @org.junit.jupiter.api.DisplayName("toDisplayName() method returns DisplayName.Valid type.")
	void toDisplayName_returns_Valid_DisplayName()
	{
		// Arrange
		ValidSearchKey searchKey = SearchKey.of("Valid Search Key").isValid().orElseThrow();

		// Act
		ValidDisplayName displayName = DisplayName.of(searchKey);

		// Assert
		assertInstanceOf(ValidDisplayName.class, displayName);
	}


	@Test @org.junit.jupiter.api.DisplayName("toDisplayName() method converts underscores to spaces.")
	void toDisplayName_converts_underscores_to_spaces()
	{
		// Arrange
		ValidSearchKey searchKey = SearchKey.of("Valid Search Key").isValid().orElseThrow();

		// Act
		DisplayName displayName = DisplayName.of(searchKey);

		// Assert
		assertEquals("Valid_Search_Key", searchKey.string());
		assertEquals("Valid Search Key", displayName.toString());
	}

}
