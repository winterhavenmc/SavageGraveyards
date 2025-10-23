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

import com.winterhavenmc.savagegraveyards.core.ports.datastore.GraveyardRepository;
import com.winterhavenmc.savagegraveyards.core.tasks.safety.InitializedSafetyManager;

import org.bukkit.plugin.java.JavaPlugin;


public record ListenerCtx(JavaPlugin plugin, MessageBuilder messageBuilder,
                          GraveyardRepository graveyards, InitializedSafetyManager safetyManager) { }
