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


import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.attributes.Attributes;
import com.winterhavenmc.savagegraveyards.plugin.models.location.ImmutableLocation;
import com.winterhavenmc.savagegraveyards.plugin.models.location.ValidLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


/**
 * Represents a graveyard, with a formatted display name, a location, and a set of attributes
 */
public sealed interface Graveyard permits Graveyard.Valid, Graveyard.Invalid
{
	DisplayName displayName();
	String worldName();

	record Invalid(DisplayName displayName, String worldName, String reason) implements Graveyard { }
	record Valid(DisplayName.Valid displayName, Attributes attributes, ValidLocation location) implements Graveyard
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

		public SearchKey.Valid searchKey()
		{
			return this.displayName.toSearchKey();
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
	static Graveyard of(final String displayName,
	                    final Attributes attributes,
						final ValidLocation location)
	{
		if (displayName == null) return new Invalid(DisplayName.NULL(), location.world().name(), "The display name was null.");
		else if (displayName.isBlank()) return new Invalid(DisplayName.BLANK(), location.world().name(), "The display name was blank.");
		else return new Valid(new DisplayName.Valid(displayName), attributes, location);
	}

}
