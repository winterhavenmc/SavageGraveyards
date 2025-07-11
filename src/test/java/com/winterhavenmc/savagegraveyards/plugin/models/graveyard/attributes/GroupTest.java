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

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.attributes.Group;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class GroupTest
{
	@Test
	void of_creates_new_Group_with_value()
	{
		// Arrange & Act
		Group result = Group.of("admin");

		// Assert
		assertEquals("admin", result.value());
	}


	@Test
	void with_creates_new_Group_with_new_value()
	{
		// Arrange
		Group group = Group.of("vip");

		// Confirm
		assertEquals("vip", group.value());

		// Act
		Group result = group.with("mod");

		// Assert
		assertEquals("mod", result.value());
	}


	@Test
	void value_retrieves_Group_value()
	{
		// Arrange
		Group group = Group.of("guest");

		// Act
		String result = group.value();

		// Assert
		assertEquals("guest", result);
	}

}
