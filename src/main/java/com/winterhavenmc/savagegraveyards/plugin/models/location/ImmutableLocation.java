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

package com.winterhavenmc.savagegraveyards.plugin.models.location;

import com.winterhavenmc.savagegraveyards.plugin.models.world.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public sealed interface ImmutableLocation permits ImmutableLocation.Valid, ImmutableLocation.Invalid
{
	record Valid(ImmutableWorld.Valid world, double x, double y, double z, float yaw, float pitch) implements ImmutableLocation { }
	record Invalid(LocationReason reason) implements ImmutableLocation { }


	static Valid of(final @NotNull Player player)
	{
		return new Valid(ImmutableWorld.of(player),
				player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
				player.getLocation().getYaw(), player.getLocation().getPitch());
	}


	static ImmutableLocation of(final Location location)
	{
		if (location == null) return new Invalid(LocationReason.LOCATION_NULL);

		return switch (ImmutableWorld.of(location.getWorld()))
		{
			case ImmutableWorld.Invalid ignored -> new Invalid(LocationReason.WORLD_INVALID);
			case ImmutableWorld.Unavailable unavailableWorld -> new Valid(unavailableWorld,
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			case ImmutableWorld.Available availableWorld -> new Valid(availableWorld,
					location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		};
	}


	static ImmutableLocation of(final String worldName, final UUID worldUid,
	                            final double x, final double y, final double z,
	                            final float yaw, final float pitch)
	{
		if (worldName == null) return new Invalid(LocationReason.WORLD_NAME_NULL);
		else if (worldName.isBlank()) return new Invalid(LocationReason.WORLD_NAME_BLANK);
		else if (worldUid == null) return new Invalid(LocationReason.WORLD_UUID_NULL);
		else return switch (ImmutableWorld.of(worldName, worldUid))
		{
			case ImmutableWorld.Invalid ignored -> new Invalid(LocationReason.WORLD_INVALID);
			case ImmutableWorld.Unavailable unavailableWorld -> new Valid(unavailableWorld, x, y, z, yaw, pitch);
			case ImmutableWorld.Available availableWorld -> new Valid(availableWorld, x, y, z, yaw, pitch);
		};
	}


}
