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

package com.winterhavenmc.savagegraveyards.plugin.models.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public sealed interface ImmutableWorld permits ImmutableWorld.Valid, ImmutableWorld.Invalid
{
	record Invalid(WorldReason reason) implements ImmutableWorld { }
	sealed interface Valid extends ImmutableWorld permits Available, Unavailable
	{
		UUID uid();
		String name();
	}
	record Available(String name, UUID uid) implements Valid { }
	record Unavailable(String name, UUID uid) implements Valid { }


	static Valid of(final @NotNull Player player)
	{
		return new Available(player.getWorld().getName(), player.getWorld().getUID());
	}


	static ImmutableWorld of(final World world)
	{
		if (world == null) return new Invalid(WorldReason.WORLD_NULL);
		else return ImmutableWorld.of(world.getName(), world.getUID());
	}


	static ImmutableWorld of(final String name, final UUID uid)
	{
		if (name == null) return new Invalid(WorldReason.WORLD_NAME_NULL);
		else if (name.isBlank()) return new Invalid(WorldReason.WORLD_NAME_BLANK);
		else if (uid == null) return new Invalid(WorldReason.WORLD_UUID_NULL);
		else if (Bukkit.getWorld(uid) == null) return new Unavailable(name, uid);
		else return new Available(name, uid);
	}


}
