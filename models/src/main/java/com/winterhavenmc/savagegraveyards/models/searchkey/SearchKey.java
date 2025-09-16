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

import com.winterhavenmc.savagegraveyards.models.displayname.DisplayName;
import com.winterhavenmc.savagegraveyards.models.displayname.ValidDisplayName;
import org.bukkit.ChatColor;

import java.util.List;


/**
 * Represents a unique search key for a graveyard, for selecting records from the datastore
 * that have been normalized by removing color codes and replacing spaces with underscores.
 */
public sealed interface SearchKey permits ValidSearchKey, InvalidSearchKey
{
	String string();


	static SearchKey of(final List<String> args)
	{
		return SearchKey.of(String.join("_", args));
	}


	static SearchKey of(final String string)
	{
		if (string == null) return new InvalidSearchKey("∅", SearchKeyFailReason.STRING_NULL);
		else if (string.isBlank()) return new InvalidSearchKey("⬚", SearchKeyFailReason.STRING_BLANK);
		else return new ValidSearchKey(ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', string))
					.replace(" ", "_"));
	}


	default DisplayName toDisplayName()
	{
		return new ValidDisplayName(this.string().replace("_", " "));
	}
}
