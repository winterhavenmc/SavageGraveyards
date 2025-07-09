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

package com.winterhavenmc.savagegraveyards.models.location.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public sealed interface ImmutableWorld permits ValidWorld, InvalidWorld
{
	static ImmutableWorld of(final String name, final UUID uid)
	{
		if (name == null) return new InvalidWorld("The world name was null.");
		else if (name.isBlank()) return new InvalidWorld("The world name was blank");
		else if (uid == null) return new InvalidWorld("The world UUID was null.");
		else if (Bukkit.getWorld(uid) == null) return new UnavailableWorld(name, uid);
		else return new AvailableWorld(name, uid);
	}


	static ImmutableWorld of(final World world)
	{
		if (world == null) return new InvalidWorld("The world was null.");
		else if (world.getName().isBlank()) return new InvalidWorld("The world name was blank.");
		else return new AvailableWorld(world.getName(), world.getUID());
	}


	static ValidWorld of(final @NotNull Player player)
	{
		return new AvailableWorld(player.getWorld().getName(), player.getWorld().getUID());
	}
}
