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
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;


class SearchKeyFailReasonTest
{
	@Test
	void getLocalizedMessage()
	{
		assertEquals("The parameter ‘{0}’ was null.", FailReason.PARAMETER_NULL.getLocalizedMessage(Locale.US));
	}

	@Test
	void testToString()
	{
		assertEquals("The parameter ‘{0}’ was null.", FailReason.PARAMETER_NULL.toString());
	}

}
