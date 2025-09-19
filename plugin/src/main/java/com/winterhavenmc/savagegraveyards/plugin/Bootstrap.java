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

import com.winterhavenmc.savagegraveyards.adapters.commands.bukkit.BukkitCommandManager;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.savagegraveyards.adapters.listeners.bukkit.BukkitPlayerEventListener;
import com.winterhavenmc.savagegraveyards.core.SavageGraveyardsPluginController;
import com.winterhavenmc.savagegraveyards.core.ports.commands.CommandManager;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.PluginController;

import com.winterhavenmc.savagegraveyards.core.ports.listeners.PlayerEventListener;
import com.winterhavenmc.savagegraveyards.core.tasks.DiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.GraveyardDiscoveryObserver;
import org.bukkit.plugin.java.JavaPlugin;


public class Bootstrap extends JavaPlugin
{
	PluginController pluginController;
	ConnectionProvider connectionProvider;
	CommandManager commandManager;
	PlayerEventListener playerEventListener;
	public DiscoveryObserver discoveryObserver;



	@Override
	public void onEnable()
	{
		pluginController = new SavageGraveyardsPluginController();
		connectionProvider = new SqliteConnectionProvider(this);
		commandManager = new BukkitCommandManager();
		playerEventListener = new BukkitPlayerEventListener();
		discoveryObserver = new GraveyardDiscoveryObserver();

		pluginController.startUp(this, connectionProvider, commandManager, playerEventListener, discoveryObserver);
	}


	@Override
	public void onDisable()
	{
		pluginController.shutDown();
	}
}
