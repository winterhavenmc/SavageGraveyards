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

package com.winterhavenmc.savagegraveyards.core;

import com.winterhavenmc.savagegraveyards.core.ports.commands.CommandDispatcher;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.core.ports.listeners.PlayerEventListener;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.DiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.SafetyManager;
import com.winterhavenmc.savagegraveyards.core.util.*;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.library.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.library.worldmanager.WorldManager;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to allow creation of graveyard locations where players
 * will respawn on death. The nearest graveyard location that is valid
 * for the player will be chosen at the time of death.
 */
public class SavageGraveyardsPluginController implements PluginController
{
	public MessageBuilder messageBuilder;
	public ConnectionProvider datastore;
	public GraveyardRepository graveyards;
	public DiscoveryRepository discoveries;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public SafetyManager safetyManager;
	public DiscoveryObserver discoveryObserver;
	public CommandDispatcher commandDispatcher;
	public PlayerEventListener playerEventListener;


	@Override
	public void startUp(final JavaPlugin plugin, final ConnectionProvider connectionProvider,
	                    final CommandDispatcher commandDispatcher, final PlayerEventListener playerEventListener,
	                    final DiscoveryObserver discoveryObserver, final SafetyManager safetyManager)
	{
		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// instantiate message builder
		this.messageBuilder = MessageBuilder.create(plugin);

		// instantiate sound configuration
		this.soundConfig = new YamlSoundConfiguration(plugin);

		// instantiate world manager
		this.worldManager = new WorldManager(plugin);

		// connect to storage object
		this.datastore = connectionProvider.connect();


		// instantiate context containers
		CommandContextContainer commandCtx = new CommandContextContainer(plugin, messageBuilder, soundConfig,
				worldManager, datastore.graveyards(), datastore.discoveries(), discoveryObserver);
		ListenerContextContainer listenerCtx = new ListenerContextContainer(plugin, messageBuilder, worldManager,
				datastore.graveyards(), safetyManager);
		SafetyContextContainer safetyCtx = new SafetyContextContainer(plugin, messageBuilder);
		DiscoveryContextContainer discoveryCtx = new DiscoveryContextContainer(plugin, messageBuilder, soundConfig,
				datastore.discoveries(), datastore.graveyards());
		MetricsContextContainer metricsCtx = new MetricsContextContainer(plugin, datastore.graveyards());

		// initialize managers
		this.commandDispatcher = commandDispatcher.init(commandCtx);
		this.playerEventListener = playerEventListener.init(listenerCtx);
		this.safetyManager = safetyManager.init(safetyCtx);
		this.discoveryObserver = discoveryObserver.init(discoveryCtx);

		// initialize metrics handler
		new MetricsHandler(metricsCtx);
	}


	@Override
	public void shutDown()
	{
		discoveryObserver.cancel();
		datastore.close();
	}

}
