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

package com.winterhavenmc.savagegraveyards.core.storage;

import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;

import org.bukkit.plugin.Plugin;


/**
 * Datastore interface
 */
public class Datastore implements AutoCloseable
{
	private final ConnectionProvider connectionProvider;


	/**
	 * Private constructor
	 */
	private Datastore(final ConnectionProvider connectionProvider)
	{
		this.connectionProvider = connectionProvider;
	}


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param plugin reference to plugin main class
	 * @return a new datastore instance of the given type
	 */
	public static Datastore connect(final Plugin plugin, final ConnectionProvider connectionProvider)
	{
		// initialize data store
		try
		{
			connectionProvider.connect();
		}
		catch (Exception exception)
		{
			plugin.getLogger().severe("Could not initialize the datastore!");
			plugin.getLogger().severe(exception.getLocalizedMessage());
		}

		// return initialized data store
		return new Datastore(connectionProvider);
	}


	/**
	 * Close datastore connection
	 */
	public void close()
	{
		connectionProvider.close();
	}


	/**
	 * Passthrough method returns the discovery repository
	 *
	 * @return the {@link DiscoveryRepository}
	 */
	public DiscoveryRepository discoveries()
	{
		return connectionProvider.discoveries();
	}


	/**
	 * Passthrough method returns the graveyard repository
	 *
	 * @return the {@link GraveyardRepository}
	 */
	public GraveyardRepository graveyards()
	{
		return connectionProvider.graveyards();
	}

}
