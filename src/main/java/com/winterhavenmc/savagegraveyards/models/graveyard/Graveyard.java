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

package com.winterhavenmc.savagegraveyards.models.graveyard;


import com.winterhavenmc.savagegraveyards.models.graveyard.attributes.Attributes;
import com.winterhavenmc.savagegraveyards.models.location.ImmutableLocation;
import com.winterhavenmc.savagegraveyards.models.location.ValidLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;


/**
 * Represents a graveyard location
 */
public sealed interface Graveyard permits Graveyard.Valid, Graveyard.Invalid
{
	String displayName();
	String worldName();

	record Invalid(String displayName, String worldName, String reason) implements Graveyard { }
	record Valid(String displayName, Attributes attributes, ValidLocation location) implements Graveyard
	{
		public Location getLocation()
		{
			return new Location(Bukkit.getWorld(location.world().uid()),
					location.x(), location.y(), location.z(), location.yaw(), location.pitch());
		}

		public String worldName()
		{
			return location().world().name();
		}
	}


	/**
	 * Creates a graveyard of the appropriate subtype when passed a minimal set of parameters.
	 * Used primarily in response to events or commands. Additional field values are retrieved
	 * from the plugin configuration, or default values are provided.
	 */
	static Graveyard of(final Plugin plugin,
	                    final String displayName,
	                    final Player player)
	{
		if (plugin == null) throw new IllegalArgumentException("The parameter 'plugin' cannot be null.");
		else if (player == null) throw new IllegalArgumentException("The parameter 'player' cannot be null.");
		else return Graveyard.of(displayName, new Attributes(plugin), ImmutableLocation.of(player));
	}


	/**
	 * Creates a graveyard of the appropriate subtype when passed a full set of parameters.
	 * Used primarily for creating objects from a persistent storage record.
	 */
	static Graveyard of(String displayName,
	                    final Attributes attributes,
						final ValidLocation location)
	{
		if (displayName == null) return new Invalid("∅", location.world().name(), "The display name was null.");
		else if (displayName.isBlank()) return new Invalid("⬚", location.world().name(), "The display name was blank");
		else return new Valid(displayName, attributes, location);
	}


	/**
	 * A derived field that generates a search key from the graveyard display name on access.
	 * Key is formed from a Display name value with color codes removed and all spaces replaced with underscores.
	 * <p>
	 * <strong>Note:</strong> preserves case
	 *
	 * @return a valid search key
	 */
	default String searchKey()
	{
		return searchKey(this.displayName());
	}


	/**
	 * Static method to create search key from graveyard display name;
	 * strips color codes and replaces spaces with underscores;
	 * <p>
	 * <strong>Note:</strong> preserves case
	 *
	 * @param displayName the graveyard display name
	 * @return String - a search key derived from graveyard search key
	 */
	static String searchKey(final String displayName)
	{
		return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', displayName).replace(' ', '_'));
	}


	/**
	 * Static method to create search key from a list of strings concatenated with underscores.
	 * strips color codes and replaces spaces with underscores;
	 * <p>
	 * <strong>Note:</strong> preserves case
	 *
	 * @param args the list of words to create a graveyard search key
	 * @return String - a search key derived from graveyard search key
	 */
	static String searchKey(List<String> args)
	{
		return searchKey(String.join("_", args));
	}

}
