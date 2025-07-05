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


import com.winterhavenmc.savagegraveyards.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.UUID;


/**
 * Represents a graveyard location
 */
public sealed interface Graveyard permits Graveyard.Invalid, Graveyard.Valid
{
	record Invalid(String reason) implements Graveyard { }
	record Valid(String searchKey, String displayName, boolean enabled, boolean hidden,
	             int discoveryRange, String discoveryMessage, String respawnMessage, String group,
	             int safetyRange, Duration safetyTime, String worldName, UUID worldUid,
	             double x, double y, double z, float yaw, float pitch) implements Graveyard {

		public Location getLocation()
		{
			return new Location(Bukkit.getWorld(worldUid), x, y, z, yaw, pitch);
		}
	}


	static Graveyard of(Plugin plugin, final String displayName, final Location location)
	{
		if (displayName == null) return new Invalid("The display name was null.");
		else if (displayName.isBlank()) return new Invalid("The display name was blank");
		else if (location == null) return new Invalid("The location was null.");
		else if (location.getWorld() == null) return new Invalid("The location world was null");
		else return new Valid(createSearchKey(displayName), displayName,
					Config.DEFAULT_ENABLED.getBoolean(plugin.getConfig()),
					Config.DEFAULT_HIDDEN.getBoolean(plugin.getConfig()),
					Config.DISCOVERY_RANGE.getInt(plugin.getConfig()),
					"", "", "", 50, Duration.ZERO,
					location.getWorld().getName(), location.getWorld().getUID(),
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}


	static Graveyard of(String searchKey, String displayName, boolean enabled, boolean hidden,
	                    int discoveryRange, String discoveryMessage, String respawnMessage, String group,
	                    int safetyRange, Duration safetyTime, String worldName, UUID worldUid,
	                    double x, double y, double z, float yaw, float pitch)
	{
		if (searchKey == null) return new Invalid("The search key was null.");
		else if (searchKey.isBlank()) return new Invalid("The search key was blank.");
		else if (displayName == null) return new Invalid("The display name was null.");
		else if (displayName.isBlank()) return new Invalid("The display name was blank");
		else if (worldName == null) return new Invalid("The world name was null.");
		else if (worldName.isBlank()) return new Invalid("The world name was blank.");
		else if (worldUid == null) return new Invalid("The world UUID was null)");
		else
		{
			return new Graveyard.Valid(searchKey, displayName, enabled, hidden,
					discoveryRange, discoveryMessage, respawnMessage, group, safetyRange, safetyTime,
					worldName, worldUid, x, y, z, yaw, pitch);
		}
	}


	/**
	 * Static method to create search key from graveyard display name;
	 * strips color codes and replaces spaces with underscores;
	 * preserves case
	 *
	 * @param displayName the graveyard display name
	 * @return String - a search key derived from graveyard display name
	 */
	static String createSearchKey(final String displayName)
	{
		String displayNameCopy = ChatColor.translateAlternateColorCodes('&', displayName);
		return ChatColor.stripColor(displayNameCopy.replace(' ', '_'));
	}

}
