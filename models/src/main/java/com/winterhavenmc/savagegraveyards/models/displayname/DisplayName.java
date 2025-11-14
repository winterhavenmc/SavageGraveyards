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
	/**
	 * Creates an instance of a {@code DisplayName} after parameter validation
	 *
	 * @param string the String to use as the graveyard display name
	 * @return a validated instance of DisplayName
	 */
	static DisplayName of(final String string)
	{
		if (string == null) return DisplayName.NULL();
		else if (string.isBlank()) return DisplayName.BLANK();
		else return new ValidDisplayName(transform(string));
	}


	/**
	 * Creates an instance of a {@code DisplayName} after parameter validation
	 *
	 * @param args a List of strings to be concatenated for use as a display name
	 * @return a validated instance of DisplayName
	 */
	static DisplayName of(final List<String> args)
	{
		if (args == null) return DisplayName.NULL();
		else if (args.isEmpty()) return DisplayName.BLANK();
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


	/**
	 * @return an {@link InvalidDisplayName} with a null symbol string for name
	 */
	static DisplayName NULL()
	{
		return new InvalidDisplayName("∅", FailReason.PARAMETER_NULL, Parameter.DISPLAY_NAME);
	}


	/**
	 * @return an {@link InvalidDisplayName} with a blank symbol string for name
	 */
	static DisplayName BLANK()
	{
		return new InvalidDisplayName("⬚", FailReason.PARAMETER_BLANK, Parameter.DISPLAY_NAME);
	}

}
