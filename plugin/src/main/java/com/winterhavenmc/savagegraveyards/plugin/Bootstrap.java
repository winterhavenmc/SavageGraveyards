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

package com.winterhavenmc.savagegraveyards.plugin;

import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.savagegraveyards.adapters.listeners.bukkit.BukkitPlayerEventListener;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.PluginController;

import com.winterhavenmc.savagegraveyards.core.ports.datastore.PlayerEventListener;
import org.bukkit.plugin.java.JavaPlugin;


public class Bootstrap extends JavaPlugin
{
	PluginController pluginController;
	ConnectionProvider connectionProvider;
	PlayerEventListener playerEventListener;


	@Override
	public void onEnable()
	{
		pluginController = new PluginController();
		connectionProvider = new SqliteConnectionProvider(this);
		playerEventListener = new BukkitPlayerEventListener();
		pluginController.startUp(this, connectionProvider, playerEventListener);
	}


	@Override
	public void onDisable()
	{
		pluginController.shutDown();
	}
}
