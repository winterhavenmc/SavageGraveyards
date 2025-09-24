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

import com.winterhavenmc.savagegraveyards.adapters.commands.bukkit.BukkitCommandDispatcher;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.savagegraveyards.adapters.listeners.bukkit.BukkitPlayerEventListener;
import com.winterhavenmc.savagegraveyards.core.controller.ControllerFailReason;
import com.winterhavenmc.savagegraveyards.core.controller.InvalidPluginController;
import com.winterhavenmc.savagegraveyards.core.controller.ValidPluginController;
import com.winterhavenmc.savagegraveyards.core.controller.PluginController;

import com.winterhavenmc.savagegraveyards.core.ports.commands.CommandDispatcher;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.ports.listeners.PlayerEventListener;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.DiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.SafetyManager;

import org.bukkit.plugin.java.JavaPlugin;


public class Bootstrap extends JavaPlugin
{
	PluginController pluginController; // core controller
	DiscoveryObserver discoveryObserver; // core task
	SafetyManager safetyManager; // core task
	ConnectionProvider connectionProvider; // core port
	CommandDispatcher commandDispatcher; // core port
	PlayerEventListener playerEventListener; // core port


	@Override
	public void onEnable()
	{
		pluginController = PluginController.create(this); // core controller
		discoveryObserver = DiscoveryObserver.create(); // core task
		safetyManager = SafetyManager.create(); // core task
		connectionProvider = SqliteConnectionProvider.create(this); // adapter
		commandDispatcher = BukkitCommandDispatcher.create(this); // adapter
		playerEventListener = BukkitPlayerEventListener.create(); // adapter

		switch (pluginController)
		{
			case ValidPluginController validPluginController ->
					validPluginController.startUp(connectionProvider, commandDispatcher, playerEventListener, discoveryObserver, safetyManager);

			case InvalidPluginController(ControllerFailReason reason) ->
			{
				this.getLogger().severe("A valid plugin controller could not be created: " + reason);
				this.getServer().getPluginManager().disablePlugin(this);
			}
		}
	}


	@Override
	public void onDisable()
	{
		if (pluginController instanceof ValidPluginController validPluginController)
		{
			validPluginController.shutDown();
		}
	}
}
