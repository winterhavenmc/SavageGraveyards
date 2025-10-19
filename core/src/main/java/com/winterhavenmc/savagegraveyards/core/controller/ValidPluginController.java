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

package com.winterhavenmc.savagegraveyards.core.controller;

import com.winterhavenmc.savagegraveyards.core.context.*;

import com.winterhavenmc.savagegraveyards.core.ports.commands.CommandDispatcher;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.ports.listeners.EventListener;

import com.winterhavenmc.savagegraveyards.core.tasks.discovery.*;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.InitializedSafetyManager;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.InvalidSafetyManager;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.SafetyManager;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.UninitializedSafetyManager;

import com.winterhavenmc.savagegraveyards.core.util.MetricsHandler;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to allow creation of graveyard locations where players
 * will respawn on death. The nearest graveyard location that is valid
 * for the player will be chosen at the time of death.
 */
public non-sealed class ValidPluginController implements PluginController
{
	private final JavaPlugin plugin;
	private final MessageBuilder messageBuilder;

	private ConnectionProvider datastore;
	private DiscoveryObserver discoveryObserver;

	public CommandDispatcher commandDispatcher;
	public EventListener eventListener;


	public ValidPluginController(final JavaPlugin plugin)
	{
		this.plugin = plugin;

		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// instantiate message builder
		this.messageBuilder = MessageBuilder.create(plugin);
	}


	public void startUp(final ConnectionProvider connectionProvider,
	                    final CommandDispatcher commandDispatcher,
	                    final EventListener eventListener,
	                    final DiscoveryObserver discoveryObserver,
	                    final SafetyManager safetyManager)
	{
		// connect to data store
		this.datastore = connectionProvider.connect();

		// initialize discovery observer
		this.discoveryObserver = init(discoveryObserver);

		// initialize command dispatcher (depends on initialized discovery observer)
		this.commandDispatcher = init(commandDispatcher, this.discoveryObserver);

		// initialize player event listener (depends on initialized safety manager)
		this.eventListener = init(eventListener, init(safetyManager));

		// instantiate metrics handler
		final MetricsCtx metricsCtx = new MetricsCtx(plugin, datastore.graveyards());
		new MetricsHandler(metricsCtx);
	}


	public void shutDown()
	{
		if (this.discoveryObserver instanceof InitializedDiscoveryObserver initializedDiscoveryObserver)
		{
			initializedDiscoveryObserver.cancel();
		}
		datastore.close();
	}


	private DiscoveryObserver init(final DiscoveryObserver discoveryObserver)
	{
		final DiscoveryCtx discoveryCtx = new DiscoveryCtx(plugin, messageBuilder, datastore.discoveries(), datastore.graveyards());
		DiscoveryObserver validatedDiscoveryObserver = switch (discoveryObserver)
		{
			case InitializedDiscoveryObserver initialized -> initialized;
			case UninitializedObserver uninitialized -> uninitialized.init(discoveryCtx);
			case InvalidDiscoveryObserver ignored -> new InvalidDiscoveryObserver("DiscoveryObserver could not be initialized!");
		};

		if (this.discoveryObserver instanceof InvalidDiscoveryObserver(String reason))
		{
			plugin.getLogger().severe(reason);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		return validatedDiscoveryObserver;
	}


	private SafetyManager init(final SafetyManager safetyManager)
	{
		final SafetyCtx safetyCtx = new SafetyCtx(plugin, messageBuilder);
		SafetyManager validatedSafetyManager = switch (safetyManager)
		{
			case InitializedSafetyManager initialized -> initialized;
			case UninitializedSafetyManager uninitialized -> uninitialized.init(safetyCtx);
			case InvalidSafetyManager ignored -> new InvalidSafetyManager("SafetyManager could not be initialized!");
		};

		if (validatedSafetyManager instanceof InvalidSafetyManager(String reason))
		{
			plugin.getLogger().severe(reason);
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		}

		return validatedSafetyManager;
	}


	private EventListener init(final EventListener eventListener,
	                           final SafetyManager safetyManager)
	{
		return (safetyManager instanceof InitializedSafetyManager initializedSafetyManager)
				? eventListener.init(new ListenerCtx(plugin, messageBuilder, datastore.graveyards(), initializedSafetyManager))
				: eventListener;
	}


	private CommandDispatcher init(final CommandDispatcher commandDispatcher,
	                               final DiscoveryObserver discoveryObserver)
	{
		return (discoveryObserver instanceof InitializedDiscoveryObserver initializedDiscoveryObserver)
				? commandDispatcher.init(new CommandCtx(plugin, messageBuilder,
						datastore.graveyards(), datastore.discoveries(), initializedDiscoveryObserver))
				: commandDispatcher;
	}

}
