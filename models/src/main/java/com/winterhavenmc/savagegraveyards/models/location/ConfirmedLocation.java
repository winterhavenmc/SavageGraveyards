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

package com.winterhavenmc.savagegraveyards.models.location;

import com.winterhavenmc.savagegraveyards.models.FailReason;
import com.winterhavenmc.savagegraveyards.models.Parameter;
import com.winterhavenmc.savagegraveyards.models.world.*;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public sealed interface ConfirmedLocation permits ValidLocation, InvalidLocation
{
	/**
	 * Create a valid confirmed location from a player location
	 *
	 * @param player the player object with the location to be used
	 * @return the valid player location wrapped in a ValidLocation object
	 */
	static ValidLocation of(final @NotNull Player player)
	{
		return new ValidLocation(ConfirmedWorld.of(player),
				player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
				player.getLocation().getYaw(), player.getLocation().getPitch());
	}


	/**
	 * Create a confirmed location from a location
	 *
	 * @param location the location to confirm
	 * @return ValidLocation if the location has a valid, loaded world; else InvalidLocation
	 */
	static ConfirmedLocation of(final Location location)
	{
		if (location == null) return new InvalidLocation(FailReason.PARAMETER_NULL, Parameter.LOCATION);

		return switch (ConfirmedWorld.of(location.getWorld()))
		{
			case InvalidWorld ignored -> new InvalidLocation(FailReason.PARAMETER_INVALID, Parameter.WORLD);
			case UnavailableWorld unavailableWorld -> new ValidLocation(unavailableWorld,
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			case AvailableWorld availableWorld -> new ValidLocation(availableWorld,
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		};
	}


	/**
	 * Create a confirmed location from raw parameter values
	 *
	 * @return ValidLocation if the worldUid references a valid, loaded world; else InvalidLocation
	 */
	static ConfirmedLocation of(final String worldName, final UUID worldUid,
	                            final double x, final double y, final double z,
	                            final float yaw, final float pitch)
	{
		if (worldName == null) return new InvalidLocation(FailReason.PARAMETER_NULL, Parameter.WORLD_NAME);
		else if (worldName.isBlank()) return new InvalidLocation(FailReason.PARAMETER_BLANK, Parameter.WORLD_NAME);
		else if (worldUid == null) return new InvalidLocation(FailReason.PARAMETER_NULL, Parameter.WORLD_UID);
		else return switch (ConfirmedWorld.of(worldName, worldUid))
		{
			case InvalidWorld ignored -> new InvalidLocation(FailReason.PARAMETER_INVALID, Parameter.WORLD);
			case UnavailableWorld unavailableWorld -> new ValidLocation(unavailableWorld, x, y, z, yaw, pitch);
			case AvailableWorld availableWorld -> new ValidLocation(availableWorld, x, y, z, yaw, pitch);
		};
	}

}
