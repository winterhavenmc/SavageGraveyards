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

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.savagegraveyards.adapters.commands.bukkit.BukkitCommandDispatcher;
import com.winterhavenmc.savagegraveyards.adapters.datastore.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.savagegraveyards.adapters.listeners.bukkit.BukkitEventListener;

import com.winterhavenmc.savagegraveyards.core.context.CommandCtx;

import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;

import com.winterhavenmc.savagegraveyards.core.tasks.discovery.InitializedDiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.InitializedSafetyManager;

import org.bukkit.plugin.java.JavaPlugin;


public class Bootstrap extends JavaPlugin
{
	private ConnectionProvider connectionProvider;


	@Override
	public void onEnable()
	{
		// install default config.yml if not present
		saveDefaultConfig();

		final MessageBuilder messageBuilder = MessageBuilder.create(this);

		this.connectionProvider = SqliteConnectionProvider.create(this);
		this.connectionProvider.connect();

		final InitializedDiscoveryObserver discoveryObserver = new InitializedDiscoveryObserver(this, messageBuilder,
				connectionProvider.discoveries(), connectionProvider.graveyards());

		final InitializedSafetyManager safetyManager = new InitializedSafetyManager(this, messageBuilder);

		final CommandCtx commandCtx = new CommandCtx(this, messageBuilder, connectionProvider.graveyards(),
				connectionProvider.discoveries(), discoveryObserver);

		new BukkitCommandDispatcher(commandCtx);
		new BukkitEventListener(this, messageBuilder, connectionProvider.graveyards(), safetyManager);
	}


	@Override
	public void onDisable()
	{
		connectionProvider.close();
	}

}
