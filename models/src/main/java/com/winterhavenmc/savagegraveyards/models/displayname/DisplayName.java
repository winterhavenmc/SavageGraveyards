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

import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.Parameter;
import org.bukkit.ChatColor;

import java.util.List;


/**
 * Represents a graveyard display name as an algebraic data type, implemented using a sealed interface
 * with permitted types of {@link ValidDisplayName} or {@link InvalidDisplayName}.
 * <p>
 * <img src="doc-files/DisplayName_structure.svg" alt="DisplayName Structure"/>
 */
public sealed interface DisplayName permits ValidDisplayName, InvalidDisplayName
{
	static DisplayName of(String string)
	{
		if (string == null) return DisplayName.NULL();
		else if (string.isBlank()) return DisplayName.BLANK();
		else return new ValidDisplayName(transform(string));
	}


	static DisplayName of(List<String> args)
	{
		if (args == null) return DisplayName.NULL();
		if (args.isEmpty()) return DisplayName.BLANK();
		else return DisplayName.of(String.join(" ", args));
	}


	private static String transform(final String string)
	{
		return string.replace("_", " ");
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
		return new InvalidDisplayName("∅", FailReason.PARAMETER_NULL, Parameter.DISPLAY_NAME);
	}


	static DisplayName BLANK()
	{
		return new InvalidDisplayName("⬚", FailReason.PARAMETER_BLANK, Parameter.DISPLAY_NAME);
	}

}
