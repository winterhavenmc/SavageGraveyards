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

import com.winterhavenmc.savagegraveyards.models.graveyard.attributes.Enabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class EnabledTest
{
	@Test
	void of_creates_new_Enabled_with_value()
	{
		// Arrange & Act
		Enabled result1 = Enabled.of(true);
		Enabled result2 = Enabled.of(false);

		// Assert
		assertTrue(result1.value());
		assertFalse(result2.value());
	}


	@Test
	void with_creates_new_Enabled_with_new_value()
	{
		// Arrange
		Enabled enabled = Enabled.of(true);

		// Confirm
		assertTrue(enabled.value());

		// Act
		Enabled result = enabled.with(false);

		// Assert
		assertFalse(result.value());
	}


	@Test
	void value_retrieves_Enabled_value()
	{
		// Arrange
		Enabled enabled = Enabled.of(true);

		// Act
		boolean result = enabled.value();

		// Assert
		assertTrue(result);
	}

}
