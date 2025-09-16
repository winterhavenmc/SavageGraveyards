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
import com.winterhavenmc.savagegraveyards.core.util.Macro;
import com.winterhavenmc.savagegraveyards.core.util.MessageId;
import com.winterhavenmc.savagegraveyards.models.graveyard.Graveyard;
import com.winterhavenmc.savagegraveyards.models.graveyard.SearchKey;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;


/**
 * Delete command implementation<br>
 * Removes graveyard record from datastore
 */
final class DeleteSubcommand extends AbstractSubcommand implements Subcommand
{
	private final PluginController.ContextContainer ctx;


	/**
	 * Class constructor
	 */
	DeleteSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "delete";
		this.usageString = "/graveyard delete <graveyard name>";
		this.description = MessageId.COMMAND_HELP_DELETE;
		this.permissionNode = "graveyard.delete";
		this.minArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender,
	                                  final Command command,
									  final String alias,
									  final String[] args)
	{
		return (args.length == 2)
				? ctx.datastore().graveyards().getMatchingNames(args[1])
				: Collections.emptyList();
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		if (!sender.hasPermission(permissionNode))
		{
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			ctx.messageBuilder().compose(sender, MessageId.PERMISSION_DENIED_DELETE).send();
			return true;
		}

		if (args.size() < minArgs)
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			displayUsage(sender);
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		switch (SearchKey.of(args))
		{
			case SearchKey.Invalid invalidKey -> invalidKeyMessage(sender, invalidKey);
			case SearchKey.Valid validKey ->
			{
				switch (ctx.datastore().graveyards().delete(validKey))
				{
					case Graveyard.Valid valid -> successMessage(sender, valid);
					case Graveyard.Invalid invalid -> notFoundMessage(sender, invalid);
				}
			}
		}
		return true;
	}


	private void invalidKeyMessage(final CommandSender sender, final SearchKey.Invalid invalid)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_DELETE_INVALID_KEY)
				.setMacro(Macro.REASON, invalid.reason())
				.send();
	}


	private void successMessage(final CommandSender sender, final Graveyard.Valid graveyard)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_SUCCESS_DELETE);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_DELETE)
				.setMacro(Macro.GRAVEYARD, graveyard)
				.send();
	}


	private void notFoundMessage(final CommandSender sender,
	                             final Graveyard.Invalid invalid)
	{
		ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_NO_RECORD)
				.setMacro(Macro.GRAVEYARD, invalid)
				.setMacro(Macro.REASON, invalid.graveyardReason())
				.send();
	}

}
