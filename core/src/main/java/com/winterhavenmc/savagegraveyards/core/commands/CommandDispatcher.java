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

package com.winterhavenmc.savagegraveyards.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;

public interface CommandDispatcher extends TabExecutor, com.winterhavenmc.savagegraveyards.core.ports.commands.CommandDispatcher
{
	/**
	 * Tab completer for SavageGraveyards commands
	 */
	@Override
	List<String> onTabComplete(@Nonnull CommandSender sender,
	                           @Nonnull Command command,
	                           @Nonnull String alias,
	                           String[] args);

	/**
	 * Command Executor for SavageGraveyards
	 */
	@Override
	boolean onCommand(@Nonnull CommandSender sender,
	                  @Nonnull Command command,
	                  @Nonnull String label,
	                  @NotNull String[] args);
}
