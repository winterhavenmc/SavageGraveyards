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

import com.winterhavenmc.savagegraveyards.commands.bukkit.BukkitCommandDispatcher;
import com.winterhavenmc.savagegraveyards.datastore.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.savagegraveyards.listeners.BukkitEventListener;
import com.winterhavenmc.savagegraveyards.metrics.BstatsMetricsHandler;
import com.winterhavenmc.savagegraveyards.tasks.discovery.BukkitDiscoveryTask;
import com.winterhavenmc.savagegraveyards.tasks.discovery.BukkitDiscoveryObserver;
import com.winterhavenmc.savagegraveyards.tasks.safety.BukkitSafetyManager;

import com.winterhavenmc.savagegraveyards.datastore.ConnectionProvider;
import com.winterhavenmc.savagegraveyards.tasks.discovery.DiscoveryObserver;
import com.winterhavenmc.savagegraveyards.tasks.discovery.DiscoveryTask;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import com.winterhavenmc.savagegraveyards.tasks.safety.SafetyManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Supplier;


/**
 * Entry point class for SavageGraveyards plugin
 */
public class Bootstrap extends JavaPlugin
{
	private ConnectionProvider connectionProvider;


	@Override
	public void onEnable()
	{
		saveDefaultConfig();

		final MessageBuilder messageBuilder = MessageBuilder.create(this);
		this.connectionProvider = SqliteConnectionProvider.create(this);
		final Supplier<DiscoveryTask> discoveryTaskSupplier = () -> BukkitDiscoveryTask.create(this, messageBuilder, connectionProvider);

		final DiscoveryObserver discoveryObserver = new BukkitDiscoveryObserver(this, discoveryTaskSupplier);
		final SafetyManager safetyManager = new BukkitSafetyManager(this, messageBuilder);

		new BukkitCommandDispatcher(this, messageBuilder, connectionProvider, discoveryObserver);
		new BukkitEventListener(this, messageBuilder, connectionProvider, safetyManager);
		new BstatsMetricsHandler(this, connectionProvider);
	}


	@Override
	public void onDisable()
	{
		connectionProvider.close();
	}

}
