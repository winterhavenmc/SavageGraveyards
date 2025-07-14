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

import org.bukkit.ChatColor;

import java.util.List;

import static com.winterhavenmc.savagegraveyards.plugin.models.graveyard.GraveyardReason.DISPLAY_NAME_STRING_BLANK;
import static com.winterhavenmc.savagegraveyards.plugin.models.graveyard.GraveyardReason.DISPLAY_NAME_STRING_NULL;


public sealed interface DisplayName permits DisplayName.Valid, DisplayName.Invalid
{
	final class Invalid implements DisplayName
	{
		private final String string;
		private final GraveyardReason reason;

		public Invalid(String string, GraveyardReason reason)
		{
			this.string = string;
			this.reason = reason;
		}

		@Override
		public String toString()
		{
			return string;
		}

		public GraveyardReason reason()
		{
			return reason;
		}
	}


	final class Valid implements DisplayName
	{
		private final String string;

		public Valid(String string)
		{
			this.string = string;
		}

		@Override
		public String toString()
		{
			return string;
		}

		public SearchKey.Valid toSearchKey()
		{
			return new SearchKey.Valid(ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', this.toString()))
					.replace(" ", "_"));
		}
	}

	static DisplayName of(String string)
	{
		if (string == null) return DisplayName.NULL();
		else if (string.isBlank()) return DisplayName.BLANK();
		else return new Valid(string);
	}

	static DisplayName of(List<String> args)
	{
		if (args == null) return DisplayName.NULL();
		if (args.isEmpty()) return DisplayName.BLANK();
		else return new Valid(String.join(" ", args));
	}

	default String colorString()
	{
		return this.toString();
	}

	default String noColor()
	{
		return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.toString()));
	}

	static DisplayName NULL()
	{
		return new Invalid("∅", DISPLAY_NAME_STRING_NULL);
	}

	static DisplayName BLANK()
	{
		return new Invalid("⬚", DISPLAY_NAME_STRING_BLANK);
	}
}
