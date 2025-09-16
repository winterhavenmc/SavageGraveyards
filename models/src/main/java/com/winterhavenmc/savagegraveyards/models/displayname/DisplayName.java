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

import org.bukkit.ChatColor;

import java.util.List;


public sealed interface DisplayName permits ValidDisplayName, InvalidDisplayName
{
	static DisplayName of(String string)
	{
		if (string == null) return DisplayName.NULL();
		else if (string.isBlank()) return DisplayName.BLANK();
		else return new ValidDisplayName(string.replace("_", " "));
	}


	static DisplayName of(List<String> args)
	{
		if (args == null) return DisplayName.NULL();
		if (args.isEmpty()) return DisplayName.BLANK();
		else return new ValidDisplayName(String.join(" ", args));
	}


	default String colorString()
	{
		return this.toString();
	}


	default String noColorString()
	{
		return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.toString()));
	}


	static DisplayName NULL()
	{
		return new InvalidDisplayName("∅", DisplayNameFailReason.STRING_NULL);
	}


	static DisplayName BLANK()
	{
		return new InvalidDisplayName("⬚", DisplayNameFailReason.STRING_BLANK);
	}

}
