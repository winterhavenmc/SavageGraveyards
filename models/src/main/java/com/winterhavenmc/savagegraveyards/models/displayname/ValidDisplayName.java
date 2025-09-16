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

package com.winterhavenmc.savagegraveyards.models.displayname;

import com.winterhavenmc.savagegraveyards.models.searchkey.ValidSearchKey;
import org.bukkit.ChatColor;


public final class ValidDisplayName implements DisplayName
{
	private final String string;


	public ValidDisplayName(String string)
	{
		this.string = string;
	}


	@Override
	public String toString()
	{
		return string;
	}


	public ValidSearchKey toSearchKey()
	{
		return new ValidSearchKey(ChatColor
				.stripColor(ChatColor.translateAlternateColorCodes('&', this.toString()))
				.replace(" ", "_"));
	}
}
