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

import com.winterhavenmc.savagegraveyards.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.core.commands.CommandManager;
import com.winterhavenmc.savagegraveyards.core.listeners.PlayerEventListener;
import com.winterhavenmc.savagegraveyards.core.tasks.DiscoveryManager;
import com.winterhavenmc.savagegraveyards.core.tasks.SafetyManager;
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
public class PluginController
{
	public MessageBuilder messageBuilder;
	public ConnectionProvider datastore;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public SafetyManager safetyManager;
	public DiscoveryManager discoveryManager;


	public void startUp(final JavaPlugin plugin, final ConnectionProvider connectionProvider)
	{
		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// instantiate message builder
		messageBuilder = MessageBuilder.create(plugin);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(plugin);

		// instantiate world manager
		worldManager = new WorldManager(plugin);

		// connect to storage object
		datastore = connectionProvider.connect();

		// instantiate safety manager
		safetyManager = new SafetyManager(plugin, messageBuilder);

		// instantiate discovery manager
		discoveryManager = new DiscoveryManager(plugin, messageBuilder, soundConfig, datastore);

		// instantiate context containers
		ListenerContextContainer listenerCtx = new ListenerContextContainer(plugin, messageBuilder, worldManager, datastore, safetyManager);
		CommandContextContainer commandCtx = new CommandContextContainer(plugin, messageBuilder, soundConfig, worldManager, datastore, discoveryManager);

		// instantiate command manager
		new CommandManager(commandCtx);

		// instantiate player event listener
		new PlayerEventListener(listenerCtx);

		// bStats
		new MetricsHandler(plugin, datastore);
	}


	public void shutDown()
	{
		discoveryManager.cancel();
		datastore.close();
	}


	public record ListenerContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder, WorldManager worldManager,
	                                       ConnectionProvider datastore, SafetyManager safetyManager) { }

	public record CommandContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder, SoundConfiguration soundConfig,
	                                      WorldManager worldManager, ConnectionProvider datastore, DiscoveryManager discoveryManager) { }
}
