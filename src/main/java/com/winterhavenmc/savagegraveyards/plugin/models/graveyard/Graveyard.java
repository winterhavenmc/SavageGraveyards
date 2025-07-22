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


import com.winterhavenmc.library.messagebuilder.pipeline.adapters.displayname.DisplayNameable;
import com.winterhavenmc.library.messagebuilder.pipeline.adapters.location.Locatable;
import com.winterhavenmc.savagegraveyards.plugin.models.graveyard.attributes.Attributes;
import com.winterhavenmc.savagegraveyards.plugin.models.location.ImmutableLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


/**
 * Represents a graveyard, with a formatted display name, a location, and a set of attributes
 */
public sealed interface Graveyard extends DisplayNameable permits Graveyard.Valid, Graveyard.Invalid
{
	DisplayName displayName();
	String worldName();

	default String getDisplayName()
	{
		return this.displayName().colorString();
	}

	record Invalid(DisplayName displayName, String worldName, GraveyardReason graveyardReason) implements Graveyard { }
	record Valid(DisplayName.Valid displayName, Attributes attributes, ImmutableLocation.Valid location) implements Graveyard, Locatable
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
	                    final DisplayName.Valid displayName,
	                    final Player player)
	{
		if (plugin == null) throw new IllegalArgumentException("The parameter 'plugin' cannot be null.");
		else if (player == null) return new Invalid(displayName, "null", GraveyardReason.PLAYER_NULL);
		else return Graveyard.of(displayName, new Attributes(plugin), ImmutableLocation.of(player));
	}


	/**
	 * Creates a graveyard of the appropriate subtype when passed a full set of parameters.
	 * Used primarily for creating objects from a persistent storage record.
	 */
	static Graveyard of(final DisplayName.Valid displayName,
	                    final Attributes attributes,
						final ImmutableLocation.Valid location)
	{
		if (displayName == null) return new Invalid(DisplayName.NULL(), location.world().name(), GraveyardReason.DISPLAY_NAME_NULL);
		else return new Valid(displayName, attributes, location);
	}

}
