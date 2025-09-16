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

package com.winterhavenmc.savagegraveyards.core.commands;

import com.winterhavenmc.savagegraveyards.core.PluginController;
import com.winterhavenmc.savagegraveyards.core.util.SoundId;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;

import org.bukkit.command.CommandSender;

import java.util.List;


/**
 * Reload command implementation<br>
 * reloads plugin configuration
 */
final class ReloadSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginController.ContextContainer ctx;


	/**
	 * Class constructor
	 */
	ReloadSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "reload";
		this.usageString = "/graveyard reload";
		this.description = MessageId.COMMAND_HELP_RELOAD;
		this.permissionNode = "graveyard.reload";
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if sender does not have permission to reload config, send error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_RELOAD).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// copy default config if not present
		ctx.plugin().saveDefaultConfig();

		// reload main configuration
		ctx.plugin().reloadConfig();

		// reload enabled worlds
		ctx.worldManager().reload();

		// reload messages
		ctx.messageBuilder().reload();

		// reload sounds
		ctx.soundConfig().reload();

		// reload Discovery manager
		ctx.discoveryManager().reload();

		// send reload success message
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_RELOAD).send();

		// player reload success message
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_RELOAD);

		// return true to suppress bukkit usage message
		return true;
	}

}
