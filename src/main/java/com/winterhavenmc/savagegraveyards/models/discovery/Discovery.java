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

import java.util.UUID;


public sealed interface Discovery permits Discovery.Valid, Discovery.Invalid
{
	String searchKey();
	record Valid(String searchKey, UUID playerUid) implements Discovery { }
	record Invalid(String searchKey, String reason) implements Discovery { }


	static Discovery of(final String searchKey, final UUID playerUid)
	{
		if (searchKey == null) return new Invalid("∅", "The search key was null.");
		else if (searchKey.isBlank()) return new Invalid("⬚", "The search key was blank.");
		else if (playerUid == null) return new Invalid(searchKey, "The player UUID was null.");
		else return new Valid(searchKey, playerUid);
	}

}
