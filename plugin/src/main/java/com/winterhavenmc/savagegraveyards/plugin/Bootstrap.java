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
import com.winterhavenmc.savagegraveyards.adapters.listeners.bukkit.BukkitEventListener;
import com.winterhavenmc.savagegraveyards.adapters.tasks.bukkit.BukkitDiscoveryTask;

import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.DiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.DiscoveryTask;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.InvalidDiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.ValidDiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.InvalidSafetyManager;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.ValidSafetyManager;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.SafetyManager;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;


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
		this.connectionProvider.connect(); // TODO: combine by making create() return pre-connected provider, or sealed-type for validation

		final Supplier<DiscoveryTask> discoveryTaskSupplier = () -> BukkitDiscoveryTask.create(this, messageBuilder,
				connectionProvider.graveyards(), connectionProvider.discoveries());

		// instantiate valid discovery observer or disable plugin
		final DiscoveryObserver discoveryObserver = DiscoveryObserver.create(this, discoveryTaskSupplier);
		switch (discoveryObserver)
		{
			// if valid, instantiate command dispatcher
			case ValidDiscoveryObserver validDiscoveryObserver -> new BukkitCommandDispatcher(this, messageBuilder,
					connectionProvider.graveyards(), connectionProvider.discoveries(), validDiscoveryObserver);
			case InvalidDiscoveryObserver invalid -> startupFailure(discoveryObserver, invalid.reason());
		}

		// instantiate valid safety manager or disable plugin
		final SafetyManager safetyManager = SafetyManager.create(this, messageBuilder);
		switch (safetyManager)
		{
			// if valid, instantiate event listener
			case ValidSafetyManager validSafetyManager -> new BukkitEventListener(this, messageBuilder,
					connectionProvider.graveyards(), validSafetyManager);
			case InvalidSafetyManager invalid -> startupFailure(safetyManager, invalid.reason());
		}
	}


	private void startupFailure(final Object object, final String reasonString)
	{
		getLogger().severe("Failed to instantiate " + object.getClass().getSimpleName() + ": " + reasonString);
		getServer().getPluginManager().disablePlugin(this);
	}


	@Override
	public void onDisable()
	{
		connectionProvider.close();
	}

}
