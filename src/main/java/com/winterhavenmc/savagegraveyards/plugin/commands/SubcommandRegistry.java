/*
 * Copyright (c) 2022-2025 Tim Savage.
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

package com.winterhavenmc.savagegraveyards.plugin.commands;

import java.util.*;


final class SubcommandRegistry
{
	final Map<String, Subcommand> subcommandMap = new LinkedHashMap<>();
	final Map<String, String> aliasMap = new HashMap<>();


	/**
	 * Register a subcommand in the map by name.
	 *
	 * @param subcommand an instance of the command
	 */
	void register(final Subcommand subcommand)
	{
		String name = subcommand.getName();
		subcommandMap.put(name.toLowerCase(), subcommand);
		subcommand.getAliases();
		for (String alias : subcommand.getAliases())
		{
			aliasMap.put(alias.toLowerCase(), name.toLowerCase());
		}
	}


	/**
	 * Get command instance from map by name
	 *
	 * @param name the command to retrieve from the map
	 * @return Subcommand - the subcommand instance, or null if no matching name
	 */
	Optional<Subcommand> getSubcommand(final String name)
	{
		String key = name.toLowerCase();
		if (aliasMap.containsKey(key))
		{
			key = aliasMap.get(key);
		}

		return Optional.ofNullable(subcommandMap.get(key));
	}


	/**
	 * Get list of keys (subcommand names) from the subcommand map
	 *
	 * @return List of String - keys of the subcommand map
	 */
	Collection<String> getKeys()
	{
		return new LinkedHashSet<>(subcommandMap.keySet());
	}

}
