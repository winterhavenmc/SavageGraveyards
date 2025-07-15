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

package com.winterhavenmc.savagegraveyards.plugin.models.world;

import com.winterhavenmc.savagegraveyards.plugin.util.Notice;

public enum WorldReason implements Notice
{
	WORLD_NULL("The world was null."),
	WORLD_NAME_NULL("The world name was null."),
	WORLD_NAME_BLANK("The world name was blank."),
	WORLD_UUID_NULL("The world UUID was null."),
	;

	private final String message;

	WorldReason(String message)
	{
		this.message = message;
	}

	@Override
	public String toString()
	{
		return this.message;
	}
}
