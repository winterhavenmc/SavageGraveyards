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

package com.winterhavenmc.savagegraveyards.core.context;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.messagebuilder.models.configuration.EnabledWorldsProvider;

import com.winterhavenmc.savagegraveyards.core.ports.datastore.DiscoveryRepository;
import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.DiscoveryObserver;

import org.bukkit.plugin.java.JavaPlugin;


public record CommandCtx(JavaPlugin plugin, MessageBuilder messageBuilder, EnabledWorldsProvider worldManager,
                         GraveyardRepository graveyards, DiscoveryRepository discoveries,
                         DiscoveryObserver discoveryObserver) { }
