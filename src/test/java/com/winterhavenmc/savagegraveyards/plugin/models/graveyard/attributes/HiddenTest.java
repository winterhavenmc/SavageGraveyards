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

import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.attributes.Hidden;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class HiddenTest
{
	@Test
	void of_creates_new_Hidden_with_value()
	{
		// Arrange & Act
		Hidden result1 = Hidden.of(true);
		Hidden result2 = Hidden.of(false);

		// Assert
		assertTrue(result1.value());
		assertFalse(result2.value());
	}


	@Test
	void with_creates_new_Hidden_with_new_value()
	{
		// Arrange
		Hidden hidden = Hidden.of(true);

		// Confirm
		assertTrue(hidden.value());

		// Act
		Hidden result = hidden.with(false);

		// Assert
		assertFalse(result.value());
	}


	@Test
	void value_retrieves_Hidden_value()
	{
		// Arrange
		Hidden hidden = Hidden.of(true);

		// Act
		boolean result = hidden.value();

		// Assert
		assertTrue(result);
	}

}
