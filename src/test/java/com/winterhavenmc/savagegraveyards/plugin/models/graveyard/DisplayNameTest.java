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

package com.winterhavenmc.savagegraveyards.plugin.models.graveyard;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class DisplayNameTest
{
	@Test @org.junit.jupiter.api.DisplayName("Of returns DisplayName.Invalid type given null string.")
	void of_returns_Invalid_given_null_string()
	{
		// Act
		DisplayName result = DisplayName.of((String) null);

		// Assert
		assertInstanceOf(DisplayName.Invalid.class, result);
		assertEquals("∅", result.toString());
		assertEquals(GraveyardReason.DISPLAY_NAME_STRING_NULL, ((DisplayName.Invalid) result).reason());
	}


	@Test @org.junit.jupiter.api.DisplayName("Of returns DisplayName.Invalid type given blank string.")
	void of_returns_Invalid_given_blank_string()
	{
		// Act
		DisplayName result = DisplayName.of("");

		// Assert
		assertInstanceOf(DisplayName.Invalid.class, result);
		assertEquals("⬚", result.toString());
		assertEquals(GraveyardReason.DISPLAY_NAME_STRING_BLANK, ((DisplayName.Invalid) result).reason());
	}


	@Test @org.junit.jupiter.api.DisplayName("Of returns DisplayName.Valid type given valid parameters.")
	void of_returns_Valid_given_valid_parameters()
	{
		// Act
		DisplayName result = DisplayName.of("Valid Display Name");

		// Assert
		assertInstanceOf(DisplayName.Valid.class, result);
	}


	@Test @org.junit.jupiter.api.DisplayName("Of returns DisplayName.Valid type given valid list of parameters.")
	void of_returns_Valid_given_valid_List_parameter()
	{
		// Act
		DisplayName result = DisplayName.of(List.of("Valid", "Display", "Name"));

		// Assert
		assertInstanceOf(DisplayName.Valid.class, result);
		assertEquals("Valid Display Name", result.toString());
	}


	@Test @org.junit.jupiter.api.DisplayName("toString() returns backing string including color codes")
	void toString_returns_backing_string_of_DisplayName()
	{
		// Arrange
		DisplayName displayName = DisplayName.of("Valid &aDisplay&r Name");

		// Act
		String result = displayName.toString();

		// Assert
		assertEquals("Valid &aDisplay&r Name", result);
	}


	@Test @org.junit.jupiter.api.DisplayName("colorString() returns backing string including color codes")
	void colorString_returns_backing_string_of_DisplayName()
	{
		// Arrange
		DisplayName displayName = DisplayName.of("Valid &aDisplay&r Name");

		// Act
		String result = displayName.colorString();

		// Assert
		assertEquals("Valid &aDisplay&r Name", result);
	}


	@Test @org.junit.jupiter.api.DisplayName("noColorString() returns backing string without color codes")
	void noColorString_returns_backing_string_of_DisplayName_without_color_codes()
	{
		// Arrange
		DisplayName displayName = DisplayName.of("Valid &aDisplay&r Name");

		// Act
		String result = displayName.noColorString();

		// Assert
		assertEquals("Valid Display Name", result);
	}


	@Test @org.junit.jupiter.api.DisplayName("toSearchKey() returns SearchKey of DisplayName")
	void toSearchKey_returns_search_string_of_DisplayName()
	{
		// Arrange
		DisplayName.Valid displayName = new DisplayName.Valid("Valid &aDisplay&r Name");
		SearchKey searchKey = displayName.toSearchKey();

		// Act
		String result = searchKey.string();

		// Assert
		assertEquals("Valid_Display_Name", result);
	}


	@Test @org.junit.jupiter.api.DisplayName("NULL() returns DisplayName.Invalid with null symbol name")
	void NULL()
	{
		// Act
		DisplayName result = DisplayName.NULL();

		// Assert
		assertInstanceOf(DisplayName.Invalid.class, result);
		assertEquals("∅", result.toString());
		assertEquals(GraveyardReason.DISPLAY_NAME_STRING_NULL, ((DisplayName.Invalid) result).reason());
	}


	@Test @org.junit.jupiter.api.DisplayName("NULL() returns DisplayName.Invalid with blank symbol name")
	void BLANK()
	{
		// Act
		DisplayName result = DisplayName.BLANK();

		// Assert
		assertInstanceOf(DisplayName.Invalid.class, result);
		assertEquals("⬚", result.toString());
		assertEquals(GraveyardReason.DISPLAY_NAME_STRING_BLANK, ((DisplayName.Invalid) result).reason());
	}

}
