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

package com.winterhavenmc.savagegraveyards.adapters.commands.bukkit;

import com.winterhavenmc.savagegraveyards.core.context.CommandCtx;
import com.winterhavenmc.savagegraveyards.core.tasks.discovery.InitializedDiscoveryObserver;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;

import org.bukkit.command.CommandSender;

import java.util.List;


/**
 * Reload command implementation<br>
 * reloads plugin configuration
 */
public final class ReloadSubcommand extends AbstractSubcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	public ReloadSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "reload";
		this.usageString = "/graveyard reload";
		this.description = MessageId.COMMAND_DESCRIPTION_RELOAD;
		this.permissionNode = "graveyard.reload";
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// check sender permission
		if (!sender.hasPermission(permissionNode))
		{
			return ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_PERMISSION_RELOAD).send();
		}

		// copy default config if not present
		ctx.plugin().saveDefaultConfig();

		// reload plugin configuration
		ctx.plugin().reloadConfig();

		// reload message builder resources
		ctx.messageBuilder().reload();

		// reload Discovery observer
		if (ctx.discoveryObserver() instanceof InitializedDiscoveryObserver initializedDiscoveryObserver)
		{
			initializedDiscoveryObserver.reload();
		}

		// send reload success message
		return ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_RELOAD).send();
	}

}
